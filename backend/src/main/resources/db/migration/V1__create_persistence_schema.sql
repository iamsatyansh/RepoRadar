create table users (
    id uuid primary key,
    username varchar(64) not null unique,
    email varchar(320) not null unique,
    password_hash varchar(255) not null,
    role varchar(16) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table repositories (
    id uuid primary key,
    github_id bigint not null unique,
    owner_login varchar(100) not null,
    name varchar(100) not null,
    full_name varchar(201) not null unique,
    html_url varchar(512) not null,
    description text,
    default_branch varchar(255),
    stars_count integer not null default 0,
    forks_count integer not null default 0,
    open_issues_count integer not null default 0,
    watchers_count integer not null default 0,
    primary_language varchar(100),
    pushed_at timestamptz,
    github_created_at timestamptz
);

create table analyses (
    id uuid primary key,
    repository_id uuid not null references repositories(id),
    requested_by uuid not null references users(id),
    status varchar(16) not null,
    analyzed_commit_sha varchar(64),
    requested_at timestamptz not null,
    completed_at timestamptz,
    error_code varchar(64),
    error_message text
);

create table language_stats (
    id uuid primary key,
    analysis_id uuid not null references analyses(id) on delete cascade,
    language_name varchar(100) not null,
    bytes bigint not null check (bytes >= 0),
    percentage numeric(5,2) not null check (percentage >= 0 and percentage <= 100),
    constraint uk_language_stats_analysis_language unique (analysis_id, language_name)
);

create table contributor_snapshots (
    id uuid primary key,
    analysis_id uuid not null references analyses(id) on delete cascade,
    github_user_id bigint,
    login varchar(100) not null,
    avatar_url varchar(512),
    commit_count integer not null check (commit_count >= 0),
    additions bigint not null default 0 check (additions >= 0),
    deletions bigint not null default 0 check (deletions >= 0),
    constraint uk_contributor_snapshot_analysis_login unique (analysis_id, login)
);

create table commit_snapshots (
    id uuid primary key,
    analysis_id uuid not null references analyses(id) on delete cascade,
    sha varchar(64) not null,
    message text not null,
    author_login varchar(100),
    committed_at timestamptz not null,
    additions bigint not null default 0 check (additions >= 0),
    deletions bigint not null default 0 check (deletions >= 0),
    constraint uk_commit_snapshot_analysis_sha unique (analysis_id, sha)
);

create table insights (
    id uuid primary key,
    analysis_id uuid not null references analyses(id) on delete cascade,
    code varchar(80) not null,
    severity varchar(16) not null,
    title varchar(180) not null,
    description text not null
);

create index idx_analyses_requested_by_requested_at on analyses (requested_by, requested_at desc);
create index idx_analyses_repository_requested_at on analyses (repository_id, requested_at desc);
create index idx_language_stats_analysis_percentage on language_stats (analysis_id, percentage desc);
create index idx_commit_snapshots_analysis_committed_at on commit_snapshots (analysis_id, committed_at desc);
