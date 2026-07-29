CREATE TABLE public.conversations (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    user_one_id character varying(255) NOT NULL,
    user_two_id character varying(255) NOT NULL
);
CREATE SEQUENCE public.conversations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER SEQUENCE public.conversations_id_seq OWNED BY public.conversations.id;
CREATE TABLE public.meeting_participants (
    meeting_id bigint NOT NULL,
    user_id character varying(255)
);
CREATE TABLE public.meetings (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    created_by_user_id character varying(255) NOT NULL,
    room_id character varying(255) NOT NULL,
    scheduled_at timestamp(6) without time zone NOT NULL,
    status character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    CONSTRAINT meetings_status_check CHECK (((status)::text = ANY ((ARRAY['SCHEDULED'::character varying, 'IN_PROGRESS'::character varying, 'ENDED'::character varying])::text[])))
);
CREATE SEQUENCE public.meetings_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER SEQUENCE public.meetings_id_seq OWNED BY public.meetings.id;
CREATE TABLE public.messages (
    id bigint NOT NULL,
    content text NOT NULL,
    conversation_id bigint NOT NULL,
    sender_id character varying(255) NOT NULL,
    sent_at timestamp(6) without time zone
);
CREATE SEQUENCE public.messages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER SEQUENCE public.messages_id_seq OWNED BY public.messages.id;
CREATE TABLE public.reports (
    id bigint NOT NULL,
    action_consommations_nulles character varying(255),
    action_estimations character varying(255),
    autre_activite_precision text,
    autre_campagne_precision text,
    autres_activites text,
    autres_documents_name character varying(255),
    autres_documents_path character varying(255),
    batteries_condensateurs_installees boolean,
    cadastre_energetique_realise boolean,
    campagnes_communication text,
    consommations_nulles_identifiees boolean,
    contraintes text,
    created_at timestamp(6) without time zone,
    created_by_user_id character varying(255) NOT NULL,
    date_index_transmis timestamp(6) without time zone,
    estimations_recensees boolean,
    guide_partage_commande boolean,
    guide_partage_performance boolean,
    illustrations_name character varying(255),
    illustrations_path character varying(255),
    index_consommation character varying(255),
    index_transmis boolean,
    modification_puissance boolean,
    nom_gestionnaire character varying(255),
    nombre_batiments integer,
    nombre_batteries_condensateurs integer,
    numero_police_senelec character varying(255),
    piece_justificative_modification_name character varying(255),
    piece_justificative_modification_path character varying(255),
    plateforme_digitale boolean,
    procedure_resiliation boolean,
    recommandations text,
    report_date timestamp(6) without time zone,
    report_status character varying(255) NOT NULL,
    service_appartenance character varying(255),
    suivi_plateforme_digitale boolean,
    updated_at timestamp(6) without time zone,
    CONSTRAINT reports_report_status_check CHECK (((report_status)::text = ANY ((ARRAY['SUBMITTED'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[])))
);
CREATE SEQUENCE public.reports_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER SEQUENCE public.reports_id_seq OWNED BY public.reports.id;
CREATE TABLE public.structures (
    id bigint NOT NULL,
    latitude character varying(255),
    longitude character varying(255),
    ministere character varying(255),
    name character varying(255) NOT NULL,
    region character varying(255),
    zone character varying(255)
);
CREATE SEQUENCE public.structures_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER SEQUENCE public.structures_id_seq OWNED BY public.structures.id;
ALTER TABLE ONLY public.conversations
    ADD CONSTRAINT conversations_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.meetings
    ADD CONSTRAINT meetings_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.messages
    ADD CONSTRAINT messages_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.reports
    ADD CONSTRAINT reports_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.structures
    ADD CONSTRAINT structures_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.meetings
    ADD CONSTRAINT uk_krspvalc39mb8j4lh7tmpdkoe UNIQUE (room_id);
ALTER TABLE ONLY public.meeting_participants
    ADD CONSTRAINT fk7uds89kvog9etbdnn653vsf6y FOREIGN KEY (meeting_id) REFERENCES public.meetings(id);
