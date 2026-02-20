-- =============================================================================
-- Campasian Advanced Features
-- Run in Supabase SQL Editor AFTER social_extensions.sql
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. IMAGE SUPPORT: Add avatar_url to profiles, image_url to posts
-- -----------------------------------------------------------------------------
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS avatar_url TEXT;
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS image_url TEXT;

-- -----------------------------------------------------------------------------
-- 2. NOTIFICATIONS: Add read_at for Mark as Read, extend types
-- -----------------------------------------------------------------------------
ALTER TABLE public.notifications ADD COLUMN IF NOT EXISTS read_at TIMESTAMPTZ;
ALTER TABLE public.notifications ADD COLUMN IF NOT EXISTS friend_request_id UUID REFERENCES public.friend_requests(id) ON DELETE SET NULL;

-- Trigger: Notify on friend request
CREATE OR REPLACE FUNCTION public.notify_on_friend_request()
RETURNS TRIGGER AS $$
DECLARE
    actor_name_val TEXT;
BEGIN
    SELECT COALESCE(p.full_name, 'Someone') INTO actor_name_val
        FROM public.profiles p WHERE p.id = NEW.from_id;
    INSERT INTO public.notifications (user_id, type, actor_id, actor_name, post_id)
        VALUES (NEW.to_id, 'friend_request', NEW.from_id, COALESCE(actor_name_val, 'Someone'), NULL);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS tr_notify_on_friend_request ON public.friend_requests;
CREATE TRIGGER tr_notify_on_friend_request
    AFTER INSERT ON public.friend_requests
    FOR EACH ROW EXECUTE FUNCTION public.notify_on_friend_request();

-- Trigger: Notify when friend request is accepted
CREATE OR REPLACE FUNCTION public.notify_on_friend_accepted()
RETURNS TRIGGER AS $$
DECLARE
    actor_name_val TEXT;
BEGIN
    IF NEW.status = 'accepted' AND (OLD.status IS NULL OR OLD.status != 'accepted') THEN
        SELECT COALESCE(p.full_name, 'Someone') INTO actor_name_val
            FROM public.profiles p WHERE p.id = NEW.to_id;
        INSERT INTO public.notifications (user_id, type, actor_id, actor_name, post_id)
            VALUES (NEW.from_id, 'friend_accepted', NEW.to_id, COALESCE(actor_name_val, 'Someone'), NULL);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS tr_notify_on_friend_accepted ON public.friend_requests;
CREATE TRIGGER tr_notify_on_friend_accepted
    AFTER UPDATE ON public.friend_requests
    FOR EACH ROW EXECUTE FUNCTION public.notify_on_friend_accepted();

-- Policy: Users can update read_at on own notifications
DROP POLICY IF EXISTS "Users can update own notifications" ON public.notifications;
CREATE POLICY "Users can update own notifications"
    ON public.notifications FOR UPDATE TO authenticated
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- -----------------------------------------------------------------------------
-- 3. MESSAGES: One-to-one chat between friends
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.messages (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_id   UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    receiver_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    content     TEXT NOT NULL,
    created_at  TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_messages_sender ON public.messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_messages_receiver ON public.messages(receiver_id);
CREATE INDEX IF NOT EXISTS idx_messages_conv ON public.messages(created_at DESC);

ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can read messages they sent or received"
    ON public.messages FOR SELECT TO authenticated
    USING (auth.uid() = sender_id OR auth.uid() = receiver_id);

CREATE POLICY "Users can send messages"
    ON public.messages FOR INSERT TO authenticated
    WITH CHECK (auth.uid() = sender_id);

-- -----------------------------------------------------------------------------
-- STORAGE BUCKETS (Supabase Dashboard: Storage -> New bucket)
-- 1. Create bucket "avatars" - Public
-- 2. Create bucket "post-images" - Public
-- 3. RLS Policies (Storage): Allow authenticated to upload/update/delete own files
--    avatars: (bucket_id = 'avatars' AND (storage.foldername(name))[1] = auth.uid()::text)
--    post-images: (bucket_id = 'post-images' AND auth.role() = 'authenticated')
-- -----------------------------------------------------------------------------
