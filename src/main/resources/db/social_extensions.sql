-- Run AFTER social_schema.sql. Adds bio, friend_requests, and profile discovery.

-- Add bio to profiles
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS bio TEXT;

-- Allow authenticated users to read all profiles (for People discovery)
DROP POLICY IF EXISTS "Users can read own profile" ON public.profiles;
CREATE POLICY "Authenticated can read all profiles"
    ON public.profiles FOR SELECT
    TO authenticated
    USING (true);

-- Friend requests table
CREATE TABLE IF NOT EXISTS public.friend_requests (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    from_id     UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    to_id       UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    status      TEXT DEFAULT 'pending',  -- pending, accepted, rejected
    created_at  TIMESTAMPTZ DEFAULT now(),
    UNIQUE(from_id, to_id)
);

CREATE INDEX IF NOT EXISTS idx_friend_requests_to ON public.friend_requests(to_id);

ALTER TABLE public.friend_requests ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can read own friend requests"
    ON public.friend_requests FOR SELECT TO authenticated
    USING (auth.uid() = from_id OR auth.uid() = to_id);

CREATE POLICY "Users can send friend requests"
    ON public.friend_requests FOR INSERT TO authenticated
    WITH CHECK (auth.uid() = from_id);

CREATE POLICY "Recipients can update or delete friend requests sent to them"
    ON public.friend_requests FOR UPDATE TO authenticated
    USING (auth.uid() = to_id)
    WITH CHECK (auth.uid() = to_id);

CREATE POLICY "Recipients can delete friend requests sent to them"
    ON public.friend_requests FOR DELETE TO authenticated
    USING (auth.uid() = to_id);

-- Allow users to update/delete own posts
CREATE POLICY "Users can update own posts"
    ON public.posts FOR UPDATE TO authenticated
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can delete own posts"
    ON public.posts FOR DELETE TO authenticated
    USING (auth.uid() = user_id);
