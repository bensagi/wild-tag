create table vulnerabilities (
    id varchar(255) not null,
    additional_references text,
    aliases text,
    cvss_info_set text,
    cvss_score float4,
    cwes text,
    description text,
    severity smallint check (severity between 0 and 4),
    source_link text,
    primary key (id)
)