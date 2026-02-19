-- =============================================================================
-- Campasian Social Media Schema
-- Run this in Supabase SQL Editor AFTER posts_table.sql and profiles_table.sql
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. LIKES (post_id, user_id)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.likes (
    post_id     UUID NOT NULL REFERENCES public.posts(id) ON DELETE CASCADE,
    user_id     UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ DEFAULT now(),
    PRIMARY KEY (post_id, user_id)
);

ALTER TABLE public.likes ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Anyone can read likes"
    ON public.likes FOR SELECT TO public USING (true);

CREATE POLICY "Authenticated users can insert likes"
    ON public.likes FOR INSERT TO authenticated
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can delete own likes"
    ON public.likes FOR DELETE TO authenticated
    USING (auth.uid() = user_id);

-- -----------------------------------------------------------------------------
-- 2. COMMENTS (post_id, user_id, content)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.comments (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id    UUID NOT NULL REFERENCES public.posts(id) ON DELETE CASCADE,
    user_id    UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    user_name  TEXT NOT NULL,
    content    TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_comments_post_id ON public.comments(post_id);

ALTER TABLE public.comments ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Anyone can read comments"
    ON public.comments FOR SELECT TO public USING (true);

CREATE POLICY "Authenticated users can insert comments"
    ON public.comments FOR INSERT TO authenticated
    WITH CHECK (auth.uid() = user_id);

-- -----------------------------------------------------------------------------
-- 3. FOLLOWS (follower_id, following_id)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.follows (
    follower_id   UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    following_id  UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at    TIMESTAMPTZ DEFAULT now(),
    PRIMARY KEY (follower_id, following_id),
    CHECK (follower_id != following_id)
);

CREATE INDEX IF NOT EXISTS idx_follows_follower ON public.follows(follower_id);
CREATE INDEX IF NOT EXISTS idx_follows_following ON public.follows(following_id);

ALTER TABLE public.follows ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Anyone can read follows"
    ON public.follows FOR SELECT TO public USING (true);

CREATE POLICY "Users can follow others"
    ON public.follows FOR INSERT TO authenticated
    WITH CHECK (auth.uid() = follower_id);

CREATE POLICY "Users can unfollow"
    ON public.follows FOR DELETE TO authenticated
    USING (auth.uid() = follower_id);

-- -----------------------------------------------------------------------------
-- 4. NOTIFICATIONS
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.notifications (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    type        TEXT NOT NULL,  -- 'like', 'comment', 'follow'
    actor_id    UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    actor_name  TEXT NOT NULL,
    post_id     UUID REFERENCES public.posts(id) ON DELETE SET NULL,
    created_at  TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON public.notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON public.notifications(created_at DESC);

ALTER TABLE public.notifications ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can read own notifications"
    ON public.notifications FOR SELECT TO authenticated
    USING (auth.uid() = user_id);

CREATE POLICY "System inserts notifications" -- via service role or authenticated insert
    ON public.notifications FOR INSERT TO authenticated
    WITH CHECK (true);

-- -----------------------------------------------------------------------------
-- 5. TRIGGERS: Create notifications on like, comment, follow
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.notify_on_like()
RETURNS TRIGGER AS $$
DECLARE
    post_owner_id UUID;
    actor_name_val TEXT;
BEGIN
    SELECT user_id INTO post_owner_id FROM public.posts WHERE id = NEW.post_id;
    IF post_owner_id IS NULL OR post_owner_id = NEW.user_id THEN RETURN NEW; END IF;
    SELECT COALESCE(p.full_name, 'Someone') INTO actor_name_val
        FROM public.profiles p WHERE p.id = NEW.user_id;
    INSERT INTO public.notifications (user_id, type, actor_id, actor_name, post_id)
        VALUES (post_owner_id, 'like', NEW.user_id, COALESCE(actor_name_val, 'Someone'), NEW.post_id);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS tr_notify_on_like ON public.likes;
CREATE TRIGGER tr_notify_on_like
    AFTER INSERT ON public.likes
    FOR EACH ROW EXECUTE FUNCTION public.notify_on_like();

CREATE OR REPLACE FUNCTION public.notify_on_comment()
RETURNS TRIGGER AS $$
DECLARE
    post_owner_id UUID;
BEGIN
    SELECT user_id INTO post_owner_id FROM public.posts WHERE id = NEW.post_id;
    IF post_owner_id IS NULL OR post_owner_id = NEW.user_id THEN RETURN NEW; END IF;
    INSERT INTO public.notifications (user_id, type, actor_id, actor_name, post_id)
        VALUES (post_owner_id, 'comment', NEW.user_id, NEW.user_name, NEW.post_id);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS tr_notify_on_comment ON public.comments;
CREATE TRIGGER tr_notify_on_comment
    AFTER INSERT ON public.comments
    FOR EACH ROW EXECUTE FUNCTION public.notify_on_comment();

CREATE OR REPLACE FUNCTION public.notify_on_follow()
RETURNS TRIGGER AS $$
DECLARE
    actor_name_val TEXT;
BEGIN
    SELECT COALESCE(p.full_name, 'Someone') INTO actor_name_val
        FROM public.profiles p WHERE p.id = NEW.follower_id;
    INSERT INTO public.notifications (user_id, type, actor_id, actor_name, post_id)
        VALUES (NEW.following_id, 'follow', NEW.follower_id, COALESCE(actor_name_val, 'Someone'), NULL);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS tr_notify_on_follow ON public.follows;
CREATE TRIGGER tr_notify_on_follow
    AFTER INSERT ON public.follows
    FOR EACH ROW EXECUTE FUNCTION public.notify_on_follow();
