SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS event_schedule_items;
DROP TABLE IF EXISTS event_images;
DROP TABLE IF EXISTS event_gamifications;
DROP TABLE IF EXISTS event_gamification;
DROP TABLE IF EXISTS vent_gamifications;

CREATE TABLE IF NOT EXISTS event_photo (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    photos VARCHAR(500) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_event_photo_event
        FOREIGN KEY (event_id) REFERENCES events(id)
        ON DELETE CASCADE
);

ALTER TABLE events DROP COLUMN IF EXISTS thumbnail;

SET FOREIGN_KEY_CHECKS = 1;
