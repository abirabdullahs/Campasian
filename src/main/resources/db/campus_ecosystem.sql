-- =============================================================================
-- Campasian Campus Ecosystem
-- Run in Supabase SQL Editor AFTER profiles_table, posts_table, social_schema, social_extensions
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. PROFILE EXTENSIONS: blood_group, session, batch
-- -----------------------------------------------------------------------------
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS blood_group TEXT;
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS session TEXT;
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS batch TEXT;

-- -----------------------------------------------------------------------------
-- 2. MARKETPLACE ITEMS (Books, Electronics, Stationery)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.marketplace_items (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    user_name   TEXT NOT NULL,
    title       TEXT NOT NULL,
    description TEXT,
    price       TEXT NOT NULL,
    condition   TEXT NOT NULL,
    category    TEXT NOT NULL,
    created_at  TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_marketplace_category ON public.marketplace_items(category);
ALTER TABLE public.marketplace_items ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Anyone can read marketplace items"
    ON public.marketplace_items FOR SELECT TO public USING (true);

CREATE POLICY "Authenticated can insert own items"
    ON public.marketplace_items FOR INSERT TO authenticated
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own items"
    ON public.marketplace_items FOR UPDATE TO authenticated
    USING (auth.uid() = user_id);

CREATE POLICY "Users can delete own items"
    ON public.marketplace_items FOR DELETE TO authenticated
    USING (auth.uid() = user_id);

-- -----------------------------------------------------------------------------
-- 3. LOST & FOUND
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.lost_found (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    user_name   TEXT NOT NULL,
    type        TEXT NOT NULL,
    title       TEXT NOT NULL,
    description TEXT,
    location    TEXT NOT NULL,
    created_at  TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_lost_found_type ON public.lost_found(type);
ALTER TABLE public.lost_found ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Anyone can read lost_found"
    ON public.lost_found FOR SELECT TO public USING (true);

CREATE POLICY "Authenticated can insert"
    ON public.lost_found FOR INSERT TO authenticated
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own lost_found"
    ON public.lost_found FOR UPDATE TO authenticated
    USING (auth.uid() = user_id);

CREATE POLICY "Users can delete own lost_found"
    ON public.lost_found FOR DELETE TO authenticated
    USING (auth.uid() = user_id);
