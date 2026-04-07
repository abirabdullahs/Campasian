-- =============================================================================
-- Allow Client-Side Blood Request Notifications (RLS)
--
-- Run this in Supabase SQL Editor if you see:
--   "new row violates row-level security policy for table \"notifications\""
--
-- This enables authenticated users to insert ONLY "blood_request" notifications,
-- while requiring actor_id = auth.uid().
-- =============================================================================

-- Inspect current policies (optional)
-- SELECT * FROM pg_policies WHERE schemaname = 'public' AND tablename = 'notifications';

ALTER TABLE public.notifications ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users can send blood requests" ON public.notifications;
CREATE POLICY "Users can send blood requests"
    ON public.notifications
    FOR INSERT
    TO authenticated
    WITH CHECK (
        type = 'blood_request'
        AND actor_id = auth.uid()
        AND user_id <> auth.uid()
    );

