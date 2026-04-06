-- Run this in Supabase SQL Editor to enable one-to-one audio call signaling.

CREATE TABLE IF NOT EXISTS public.calls (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    caller_id    UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    receiver_id  UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    status       TEXT NOT NULL CHECK (status IN ('pending', 'accepted', 'rejected', 'ended')),
    channel_name TEXT NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE public.calls ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.calls REPLICA IDENTITY FULL;
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_publication_tables
        WHERE pubname = 'supabase_realtime'
          AND schemaname = 'public'
          AND tablename = 'calls'
    ) THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.calls;
    END IF;
END $$;

DROP POLICY IF EXISTS "Users can read own calls" ON public.calls;
CREATE POLICY "Users can read own calls"
    ON public.calls FOR SELECT TO authenticated
    USING (auth.uid() = caller_id OR auth.uid() = receiver_id);

DROP POLICY IF EXISTS "Users can create outgoing calls" ON public.calls;
CREATE POLICY "Users can create outgoing calls"
    ON public.calls FOR INSERT TO authenticated
    WITH CHECK (auth.uid() = caller_id);

DROP POLICY IF EXISTS "Call participants can update calls" ON public.calls;
CREATE POLICY "Call participants can update calls"
    ON public.calls FOR UPDATE TO authenticated
    USING (auth.uid() = caller_id OR auth.uid() = receiver_id)
    WITH CHECK (auth.uid() = caller_id OR auth.uid() = receiver_id);
