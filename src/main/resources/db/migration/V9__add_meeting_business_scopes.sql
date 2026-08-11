-- 1. Add nullable type column first
ALTER TABLE public.meetings ADD COLUMN type VARCHAR(255);

-- 2. Add reference_id
ALTER TABLE public.meetings ADD COLUMN reference_id BIGINT;

-- 3. Backfill existing rows to DIRECT
UPDATE public.meetings SET type = 'DIRECT' WHERE type IS NULL;

-- 4. Make type NOT NULL
ALTER TABLE public.meetings ALTER COLUMN type SET NOT NULL;

-- 5. Add CHECK constraint for allowed type values
ALTER TABLE public.meetings ADD CONSTRAINT chk_meetings_type CHECK (
    type IN ('DIRECT', 'GLOBAL', 'COHORT', 'STRUCTURE', 'MINISTERE')
);

-- 6. Add CHECK constraint enforcing reference_id semantics
ALTER TABLE public.meetings ADD CONSTRAINT chk_meetings_reference_by_type CHECK (
    (type IN ('DIRECT', 'GLOBAL') AND reference_id IS NULL) OR
    (type IN ('COHORT', 'STRUCTURE', 'MINISTERE') AND reference_id IS NOT NULL)
);

-- 7. Add useful indexes for scope lookup
CREATE INDEX idx_meetings_type ON public.meetings (type);
CREATE INDEX idx_meetings_type_reference ON public.meetings (type, reference_id);
