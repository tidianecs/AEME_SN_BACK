-- Drop old constraints
ALTER TABLE public.conversations DROP CONSTRAINT IF EXISTS chk_conversations_type;
ALTER TABLE public.conversations DROP CONSTRAINT IF EXISTS chk_conversations_participants_by_type;

-- Recreate chk_conversations_type with MINISTERE
ALTER TABLE public.conversations
    ADD CONSTRAINT chk_conversations_type
    CHECK (type IN ('DIRECT', 'COHORT', 'STRUCTURE', 'GLOBAL', 'MINISTERE'));

-- Recreate chk_conversations_participants_by_type with MINISTERE
ALTER TABLE public.conversations
    ADD CONSTRAINT chk_conversations_participants_by_type
    CHECK (
        (
            type = 'DIRECT'
            AND user_one_id IS NOT NULL
            AND BTRIM(user_one_id) <> ''
            AND user_two_id IS NOT NULL
            AND BTRIM(user_two_id) <> ''
        )
        OR
        (
            type IN ('COHORT', 'STRUCTURE', 'GLOBAL', 'MINISTERE')
            AND user_one_id IS NULL
            AND user_two_id IS NULL
        )
    );

-- Drop old index
DROP INDEX IF EXISTS public.uq_conversations_active_group_reference;

-- Recreate index with MINISTERE
CREATE UNIQUE INDEX uq_conversations_active_group_reference
ON public.conversations(type, reference_id)
WHERE active = true AND type IN ('COHORT', 'STRUCTURE', 'MINISTERE') AND reference_id IS NOT NULL;
