-- Run this in Supabase SQL Editor to create the posts table.
-- Links to auth.users via user_id.

CREATE TABLE IF NOT EXISTS public.posts (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    user_name   TEXT NOT NULL,
    content     TEXT NOT NULL,
    university  TEXT,
    created_at  TIMESTAMPTZ DEFAULT now()
);

-- Enable RLS
ALTER TABLE public.posts ENABLE ROW LEVEL SECURITY;

-- Allow authenticated users to insert posts
CREATE POLICY "Authenticated users can insert posts"
    ON public.posts FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = user_id);

-- Allow anyone (including anon) to read posts for the feed
CREATE POLICY "Anyone can read posts"
    ON public.posts FOR SELECT
    TO public
    USING (true);
