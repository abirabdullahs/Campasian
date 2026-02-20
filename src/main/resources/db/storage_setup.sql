-- =============================================================================
-- Supabase Storage Setup for Campasian
-- =============================================================================
-- STEP 1: Dashboard -> Storage -> New bucket
--   - "avatars" (Public)
--   - "post-images" (Public)
--
-- STEP 2: Run policies below. Drop first if re-running.
-- =============================================================================

DROP POLICY IF EXISTS "Users can upload own avatar" ON storage.objects;
DROP POLICY IF EXISTS "Users can update own avatar" ON storage.objects;
DROP POLICY IF EXISTS "Authenticated can upload post images" ON storage.objects;
DROP POLICY IF EXISTS "Public read avatars" ON storage.objects;
DROP POLICY IF EXISTS "Public read post-images" ON storage.objects;

CREATE POLICY "Users can upload own avatar"
ON storage.objects FOR INSERT TO authenticated
WITH CHECK (bucket_id = 'avatars' AND (storage.foldername(name))[1] = auth.uid()::text);

CREATE POLICY "Users can update own avatar"
ON storage.objects FOR UPDATE TO authenticated
USING (bucket_id = 'avatars' AND (storage.foldername(name))[1] = auth.uid()::text);

CREATE POLICY "Authenticated can upload post images"
ON storage.objects FOR INSERT TO authenticated
WITH CHECK (bucket_id = 'post-images');

-- Allow public read (for public buckets)
CREATE POLICY "Public read avatars"
ON storage.objects FOR SELECT TO public
USING (bucket_id = 'avatars');

CREATE POLICY "Public read post-images"
ON storage.objects FOR SELECT TO public
USING (bucket_id = 'post-images');
