CREATE TABLE IF NOT EXISTS route_definitions (
    route_id VARCHAR,
    body LONGTEXT default '{}',
    PRIMARY KEY (route_id)
    );
