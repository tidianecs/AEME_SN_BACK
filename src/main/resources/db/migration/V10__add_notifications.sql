CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient_id BIGINT NOT NULL,
    meeting_id BIGINT,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    read_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id) REFERENCES profils_utilisateurs (id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_meeting FOREIGN KEY (meeting_id) REFERENCES meetings (id) ON DELETE SET NULL
);

CREATE INDEX idx_notifications_recipient_id ON notifications (recipient_id);
CREATE INDEX idx_notifications_created_at ON notifications (created_at);
CREATE INDEX idx_notifications_read_at ON notifications (read_at);
