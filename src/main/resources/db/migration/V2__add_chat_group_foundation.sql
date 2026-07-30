-- V2: Add Chat Group Foundation

-- Fix missing defaults from V1 for existing tables
ALTER TABLE public.conversations ALTER COLUMN id SET DEFAULT nextval('public.conversations_id_seq'::regclass);
ALTER TABLE public.meetings ALTER COLUMN id SET DEFAULT nextval('public.meetings_id_seq'::regclass);
ALTER TABLE public.messages ALTER COLUMN id SET DEFAULT nextval('public.messages_id_seq'::regclass);
ALTER TABLE public.reports ALTER COLUMN id SET DEFAULT nextval('public.reports_id_seq'::regclass);
ALTER TABLE public.structures ALTER COLUMN id SET DEFAULT nextval('public.structures_id_seq'::regclass);

ALTER TABLE public.conversations
    ADD COLUMN type VARCHAR(32) NOT NULL DEFAULT 'DIRECT',
    ADD COLUMN name VARCHAR(255),
    ADD COLUMN reference_id VARCHAR(255),
    ADD COLUMN created_by_user_id VARCHAR(255),
    ADD COLUMN system_managed BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN updated_at TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE public.conversations
    ADD CONSTRAINT chk_conversations_type CHECK (type IN ('DIRECT', 'COHORT', 'STRUCTURE', 'GLOBAL'));

CREATE TABLE public.conversation_members (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL DEFAULT 'MEMBER',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    joined_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    left_at TIMESTAMP WITHOUT TIME ZONE
);

ALTER TABLE public.conversation_members
    ADD CONSTRAINT chk_conversation_members_role CHECK (role IN ('MEMBER', 'GROUP_ADMIN'));

ALTER TABLE public.conversation_members
    ADD CONSTRAINT fk_conversation_members_conversation
    FOREIGN KEY (conversation_id)
    REFERENCES public.conversations(id)
    ON DELETE CASCADE;

CREATE INDEX idx_conversation_members_conversation_id ON public.conversation_members(conversation_id);
CREATE INDEX idx_conversation_members_user_active ON public.conversation_members(user_id, active);
CREATE UNIQUE INDEX uq_conversation_members_active ON public.conversation_members(conversation_id, user_id) WHERE active = true;

-- Backfill
INSERT INTO public.conversation_members (conversation_id, user_id, role, active, joined_at)
SELECT id, BTRIM(user_one_id), 'MEMBER', true, COALESCE(created_at, CURRENT_TIMESTAMP)
FROM public.conversations
WHERE user_one_id IS NOT NULL AND BTRIM(user_one_id) <> ''
ON CONFLICT DO NOTHING;

INSERT INTO public.conversation_members (conversation_id, user_id, role, active, joined_at)
SELECT id, BTRIM(user_two_id), 'MEMBER', true, COALESCE(created_at, CURRENT_TIMESTAMP)
FROM public.conversations
WHERE user_two_id IS NOT NULL AND BTRIM(user_two_id) <> ''
ON CONFLICT DO NOTHING;

-- Update updated_at
UPDATE public.conversations
SET updated_at = created_at
WHERE created_at IS NOT NULL;

-- Modify Message
ALTER TABLE public.messages
    ADD COLUMN sender_full_name VARCHAR(255);

-- Business Indexes
CREATE INDEX idx_conversations_type ON public.conversations(type);
CREATE INDEX idx_conversations_reference_id ON public.conversations(reference_id);
CREATE INDEX idx_messages_conversation_sent_at ON public.messages(conversation_id, sent_at);

CREATE UNIQUE INDEX uq_conversations_active_global ON public.conversations(type) WHERE type = 'GLOBAL' AND active = true;
CREATE UNIQUE INDEX uq_conversations_active_group_reference ON public.conversations(type, reference_id) WHERE active = true AND type IN ('COHORT', 'STRUCTURE') AND reference_id IS NOT NULL;
