-- =============================================================================
-- Profiles RLS: Allow authenticated users to read ALL profiles (People discovery)
-- Run this in Supabase SQL Editor if getAllProfiles() returns empty.
-- =============================================================================
-- The base profiles_table.sql only allows "Users can read own profile" (auth.uid() = id).
-- For People discovery, we need authenticated users to read all profiles.
--
-- Option A (recommended): Add a permissive SELECT policy (keeps RLS enabled)
-- -----------------------------------------------------------------------------
DROP POLICY IF EXISTS "Users can read own profile" ON public.profiles;
CREATE POLICY "Authenticated can read all profiles"
    ON public.profiles FOR SELECT
    TO authenticated
    USING (true);

-- Re-add "own profile" insert/update if you need stricter control on those:
-- (profiles_table.sql already has INSERT/UPDATE policies)

-- Option B: Disable RLS entirely (less secure - use only for debugging)
-- -----------------------------------------------------------------------------
-- ALTER TABLE public.profiles DISABLE ROW LEVEL SECURITY;

-- If you get "column bio does not exist", run social_extensions.sql first:
-- ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS bio TEXT;
