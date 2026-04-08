create table customer (
    customer_id bigint primary key,
    name varchar(255) not null,
    address varchar(500) not null,
    type varchar(20) not null,
    deleted_at timestamp null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table account (
    account_id bigint primary key,
    customer_id bigint not null,
    account_type varchar(20) not null,
    status varchar(20) not null,
    balance numeric(19,2) not null,
    interest_rate numeric(12,4) null,
    deleted_at timestamp null,
    version bigint not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint fk_account_customer foreign key (customer_id) references customer(customer_id)
);

create table bank_transaction (
    transaction_id varchar(36) primary key,
    account_id bigint not null,
    amount numeric(19,2) not null,
    direction varchar(20) not null,
    status varchar(20) not null,
    timestamp timestamp not null,
    description varchar(255) null,
    sender_info varchar(100) null,
    receiver_info varchar(100) null,
    idempotency_key varchar(255) null,
    constraint fk_transaction_account foreign key (account_id) references account(account_id)
);

create table idempotency_record (
    storage_key varchar(320) primary key,
    idempotency_key varchar(255) not null,
    caller_user_id varchar(64) not null,
    operation_type varchar(50) not null,
    response_status integer not null,
    response_body clob not null,
    created_at timestamp not null
);
