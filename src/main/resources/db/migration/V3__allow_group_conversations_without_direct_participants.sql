ALTER TABLE public.conversations
    ALTER COLUMN user_one_id DROP NOT NULL;

ALTER TABLE public.conversations
    ALTER COLUMN user_two_id DROP NOT NULL;

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
            type IN ('COHORT', 'STRUCTURE', 'GLOBAL')
            AND user_one_id IS NULL
            AND user_two_id IS NULL
        )
    );
