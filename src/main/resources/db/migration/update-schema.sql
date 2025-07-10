CREATE TABLE building
(
    id        UUID NOT NULL,
    name      VARCHAR(255),
    floors    INTEGER,
    longitude DECIMAL(9, 6),
    latitude  DECIMAL(9, 6),
    CONSTRAINT pk_building PRIMARY KEY (id)
);

CREATE TABLE game
(
    id                      UUID         NOT NULL,
    created_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    game_mode               VARCHAR(255) NOT NULL,
    winner_user_id          UUID,
    ranked_average_elo      INTEGER,
    multiplayer_timer       INTEGER,
    multiplayer_round_count INTEGER,
    CONSTRAINT pk_game PRIMARY KEY (id)
);

CREATE TABLE guess
(
    id          UUID             NOT NULL,
    user_id     UUID             NOT NULL,
    time        INTEGER          NOT NULL,
    guess_x     DOUBLE PRECISION NOT NULL,
    guess_y     DOUBLE PRECISION NOT NULL,
    building_id UUID,
    floor       INTEGER,
    CONSTRAINT pk_guess PRIMARY KEY (id)
);

CREATE TABLE round
(
    id       UUID NOT NULL,
    scene_id UUID,
    game_id  UUID,
    CONSTRAINT pk_round PRIMARY KEY (id)
);

CREATE TABLE round_guess
(
    id       UUID    NOT NULL,
    round_id UUID    NOT NULL,
    user_id  UUID    NOT NULL,
    guess_id UUID    NOT NULL,
    points   INTEGER NOT NULL,
    CONSTRAINT pk_roundguess PRIMARY KEY (id)
);

CREATE TABLE scene
(
    id          UUID             NOT NULL,
    image       VARCHAR(255)     NOT NULL,
    location_x  DOUBLE PRECISION NOT NULL,
    location_y  DOUBLE PRECISION NOT NULL,
    floor       INTEGER,
    building_id UUID,
    CONSTRAINT pk_scene PRIMARY KEY (id)
);

CREATE TABLE "user"
(
    id            UUID NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE,
    username      VARCHAR(255),
    email_address VARCHAR(255),
    password      VARCHAR(255),
    elo           INTEGER,
    streak        INTEGER,
    last_login_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_user PRIMARY KEY (id)
);

ALTER TABLE game
    ADD CONSTRAINT FK_GAME_WINNER_USER FOREIGN KEY (winner_user_id) REFERENCES "user" (id);

ALTER TABLE guess
    ADD CONSTRAINT FK_GUESS_BUILDING FOREIGN KEY (building_id) REFERENCES building (id);

ALTER TABLE guess
    ADD CONSTRAINT FK_GUESS_USER FOREIGN KEY (user_id) REFERENCES "user" (id);

ALTER TABLE round
    ADD CONSTRAINT FK_ROUND_GAME FOREIGN KEY (game_id) REFERENCES game (id);

ALTER TABLE round_guess
    ADD CONSTRAINT FK_ROUND_GUESS_GUESS FOREIGN KEY (guess_id) REFERENCES guess (id);

ALTER TABLE round_guess
    ADD CONSTRAINT FK_ROUND_GUESS_ROUND FOREIGN KEY (round_id) REFERENCES round (id);

ALTER TABLE round_guess
    ADD CONSTRAINT FK_ROUND_GUESS_USER FOREIGN KEY (user_id) REFERENCES "user" (id);

ALTER TABLE round
    ADD CONSTRAINT FK_ROUND_SCENE FOREIGN KEY (scene_id) REFERENCES scene (id);

ALTER TABLE scene
    ADD CONSTRAINT FK_SCENE_BUILDING FOREIGN KEY (building_id) REFERENCES building (id);
CREATE TABLE building
(
    id        UUID NOT NULL,
    name      VARCHAR(255),
    floors    INTEGER,
    longitude DECIMAL(9, 6),
    latitude  DECIMAL(9, 6),
    CONSTRAINT pk_building PRIMARY KEY (id)
);

CREATE TABLE game
(
    id                      UUID                        NOT NULL,
    created_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    game_mode               VARCHAR(255)                NOT NULL,
    winner_user_id          UUID,
    ranked_average_elo      INTEGER,
    multiplayer_timer       INTEGER,
    multiplayer_round_count INTEGER,
    CONSTRAINT pk_game PRIMARY KEY (id)
);

CREATE TABLE guess
(
    id          UUID             NOT NULL,
    user_id     UUID             NOT NULL,
    time        INTEGER          NOT NULL,
    guess_x     DOUBLE PRECISION NOT NULL,
    guess_y     DOUBLE PRECISION NOT NULL,
    building_id UUID,
    floor       INTEGER,
    CONSTRAINT pk_guess PRIMARY KEY (id)
);

CREATE TABLE round
(
    id       UUID NOT NULL,
    scene_id UUID,
    game_id  UUID,
    CONSTRAINT pk_round PRIMARY KEY (id)
);

CREATE TABLE round_guess
(
    id       UUID    NOT NULL,
    round_id UUID    NOT NULL,
    user_id  UUID    NOT NULL,
    guess_id UUID    NOT NULL,
    points   INTEGER NOT NULL,
    CONSTRAINT pk_roundguess PRIMARY KEY (id)
);

CREATE TABLE scene
(
    id          UUID             NOT NULL,
    image       VARCHAR(255)     NOT NULL,
    location_x  DOUBLE PRECISION NOT NULL,
    location_y  DOUBLE PRECISION NOT NULL,
    floor       INTEGER,
    building_id UUID,
    CONSTRAINT pk_scene PRIMARY KEY (id)
);

CREATE TABLE "user"
(
    id            UUID NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE,
    username      VARCHAR(255),
    email_address VARCHAR(255),
    password      VARCHAR(255),
    elo           INTEGER,
    streak        INTEGER,
    last_login_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_user PRIMARY KEY (id)
);

ALTER TABLE game
    ADD CONSTRAINT FK_GAME_WINNER_USER FOREIGN KEY (winner_user_id) REFERENCES "user" (id);

ALTER TABLE guess
    ADD CONSTRAINT FK_GUESS_BUILDING FOREIGN KEY (building_id) REFERENCES building (id);

ALTER TABLE guess
    ADD CONSTRAINT FK_GUESS_USER FOREIGN KEY (user_id) REFERENCES "user" (id);

ALTER TABLE round
    ADD CONSTRAINT FK_ROUND_GAME FOREIGN KEY (game_id) REFERENCES game (id);

ALTER TABLE round_guess
    ADD CONSTRAINT FK_ROUND_GUESS_GUESS FOREIGN KEY (guess_id) REFERENCES guess (id);

ALTER TABLE round_guess
    ADD CONSTRAINT FK_ROUND_GUESS_ROUND FOREIGN KEY (round_id) REFERENCES round (id);

ALTER TABLE round_guess
    ADD CONSTRAINT FK_ROUND_GUESS_USER FOREIGN KEY (user_id) REFERENCES "user" (id);

ALTER TABLE round
    ADD CONSTRAINT FK_ROUND_SCENE FOREIGN KEY (scene_id) REFERENCES scene (id);

ALTER TABLE scene
    ADD CONSTRAINT FK_SCENE_BUILDING FOREIGN KEY (building_id) REFERENCES building (id);