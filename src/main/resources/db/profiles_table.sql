-- Run this in Supabase SQL Editor to create the profiles table.
-- Links to auth.users via id (UUID).
-- RLS: Allow insert for authenticated users, select for same user.

CREATE TABLE IF NOT EXISTS public.profiles (
    id              UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    full_name       TEXT,
    university_name TEXT,
    ein_number      TEXT,
    department      TEXT,
    bio             TEXT,
    blood_group     TEXT,
    avatar_url      TEXT,
    session         TEXT,
    batch           TEXT,
    created_at      TIMESTAMPTZ DEFAULT now()
);

-- Enable RLS
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

-- Allow inserts for authenticated users (anon key can insert with valid JWT)
CREATE POLICY "Users can insert own profile"
    ON public.profiles FOR INSERT
    WITH CHECK (auth.uid() = id);

-- Allow users to read their own profile
CREATE POLICY "Users can read own profile"
    ON public.profiles FOR SELECT
    USING (auth.uid() = id);

-- Allow authenticated users to read all profiles
CREATE POLICY "Authenticated users can read all profiles"
    ON public.profiles FOR SELECT
    USING (auth.role() = 'authenticated');

-- Allow users to update their own profile
CREATE POLICY "Users can update own profile"
    ON public.profiles FOR UPDATE
    USING (auth.uid() = id);
