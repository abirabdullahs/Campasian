-- =============================================================================
-- Campasian Academic & Community Features
-- Run AFTER campus_ecosystem.sql
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. COURSE RESOURCE LIBRARY
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.course_resources (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    user_name   TEXT NOT NULL,
    title       TEXT NOT NULL,
    drive_link  TEXT,
    department  TEXT NOT NULL,
    semester    TEXT NOT NULL,
    created_at  TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_course_resources_dept ON public.course_resources(department);
CREATE INDEX IF NOT EXISTS idx_course_resources_semester ON public.course_resources(semester);
ALTER TABLE public.course_resources ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Anyone can read course_resources" ON public.course_resources FOR SELECT TO public USING (true);
CREATE POLICY "Authenticated can insert" ON public.course_resources FOR INSERT TO authenticated WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update own" ON public.course_resources FOR UPDATE TO authenticated USING (auth.uid() = user_id);
CREATE POLICY "Users can delete own" ON public.course_resources FOR DELETE TO authenticated USING (auth.uid() = user_id);

-- -----------------------------------------------------------------------------
-- 2. ANONYMOUS CONFESSIONS
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.confessions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    content     TEXT NOT NULL,
    created_at  TIMESTAMPTZ DEFAULT now()
);

ALTER TABLE public.confessions ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Anyone can read confessions" ON public.confessions FOR SELECT TO public USING (true);
CREATE POLICY "Authenticated can insert" ON public.confessions FOR INSERT TO authenticated WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can delete own" ON public.confessions FOR DELETE TO authenticated USING (auth.uid() = user_id);

-- -----------------------------------------------------------------------------
-- 3. CAMPUS EVENTS
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.campus_events (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    title       TEXT NOT NULL,
    description TEXT,
    event_date  TEXT NOT NULL,
    venue       TEXT NOT NULL,
    interested_count INTEGER DEFAULT 0,
    created_at  TIMESTAMPTZ DEFAULT now()
);

ALTER TABLE public.campus_events ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Anyone can read campus_events" ON public.campus_events FOR SELECT TO public USING (true);
CREATE POLICY "Authenticated can insert" ON public.campus_events FOR INSERT TO authenticated WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Authenticated can update campus_events" ON public.campus_events FOR UPDATE TO authenticated USING (true);

-- Event interest tracking (who is interested)
CREATE TABLE IF NOT EXISTS public.event_interests (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id    UUID NOT NULL REFERENCES public.campus_events(id) ON DELETE CASCADE,
    user_id     UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ DEFAULT now(),
    UNIQUE(event_id, user_id)
);

ALTER TABLE public.event_interests ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Anyone can read event_interests" ON public.event_interests FOR SELECT TO public USING (true);
CREATE POLICY "Authenticated can insert" ON public.event_interests FOR INSERT TO authenticated WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can delete own" ON public.event_interests FOR DELETE TO authenticated USING (auth.uid() = user_id);

-- -----------------------------------------------------------------------------
-- 4. STUDY PARTNER POSTS
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.study_partner_posts (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    user_name   TEXT NOT NULL,
    subject     TEXT NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ DEFAULT now()
);

ALTER TABLE public.study_partner_posts ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Anyone can read study_partner_posts" ON public.study_partner_posts FOR SELECT TO public USING (true);
CREATE POLICY "Authenticated can insert" ON public.study_partner_posts FOR INSERT TO authenticated WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can delete own" ON public.study_partner_posts FOR DELETE TO authenticated USING (auth.uid() = user_id);

-- -----------------------------------------------------------------------------
-- 5. FACULTY DIRECTORY & FEEDBACK
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.faculty (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        TEXT NOT NULL,
    department  TEXT NOT NULL,
    email       TEXT
);

CREATE INDEX IF NOT EXISTS idx_faculty_dept ON public.faculty(department);
ALTER TABLE public.faculty ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Anyone can read faculty" ON public.faculty FOR SELECT TO public USING (true);
CREATE POLICY "Authenticated can insert faculty" ON public.faculty FOR INSERT TO authenticated USING (true);

-- Seed sample faculty (run once; add more via Supabase dashboard)
DO $$
BEGIN
  IF (SELECT COUNT(*) FROM public.faculty) = 0 THEN
    INSERT INTO public.faculty (name, department, email) VALUES
      ('Dr. Ahmed Rahman', 'CSE', 'ahmed.rahman@uni.edu'),
      ('Dr. Fatima Khan', 'EEE', 'fatima.khan@uni.edu'),
      ('Prof. Karim Hossain', 'BBA', 'karim.hossain@uni.edu');
  END IF;
END $$;

-- Faculty feedback (ratings / tips)
CREATE TABLE IF NOT EXISTS public.faculty_feedback (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    faculty_id  UUID NOT NULL REFERENCES public.faculty(id) ON DELETE CASCADE,
    user_id     UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    rating      INTEGER,
    feedback    TEXT,
    created_at  TIMESTAMPTZ DEFAULT now()
);

ALTER TABLE public.faculty_feedback ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Anyone can read faculty_feedback" ON public.faculty_feedback FOR SELECT TO public USING (true);
CREATE POLICY "Authenticated can insert" ON public.faculty_feedback FOR INSERT TO authenticated WITH CHECK (auth.uid() = user_id);
