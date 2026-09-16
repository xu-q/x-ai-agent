-- ============================================================
-- AI 对话相关表（PostgreSQL 语法）
-- 数据库：application-local.yml 中的 ai_agent
-- 字符集：数据库默认即可（UTF-8）
-- ============================================================

-- ---------- AI 对话消息表（单表，按 conversation_id 区分会话） ----------
CREATE TABLE chat_message (
  id              BIGSERIAL PRIMARY KEY,
  user_id         BIGINT        NOT NULL DEFAULT 0,
  conversation_id VARCHAR(36)   NOT NULL,
  role            VARCHAR(10)    NOT NULL,
  content         TEXT,
  create_time     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted         SMALLINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_chat_message_conversation_id ON chat_message (conversation_id);
CREATE INDEX idx_chat_message_user_id        ON chat_message (user_id);
CREATE INDEX idx_chat_message_create_time     ON chat_message (create_time);

COMMENT ON TABLE  chat_message                  IS 'AI 对话消息表';
COMMENT ON COLUMN chat_message.id               IS '主键(自增)';
COMMENT ON COLUMN chat_message.user_id          IS '用户ID，0=匿名';
COMMENT ON COLUMN chat_message.conversation_id  IS '会话ID(UUID)，新建对话时生成';
COMMENT ON COLUMN chat_message.role             IS 'user / assistant / system';
COMMENT ON COLUMN chat_message.content          IS '消息内容（可能较长）';
COMMENT ON COLUMN chat_message.create_time      IS '创建时间';
COMMENT ON COLUMN chat_message.deleted          IS '逻辑删除 0未删 1已删';

-- ---------- 消息引用的知识片段（RAG 引用落库，存快照） ----------
CREATE TABLE chat_message_citation (
  id            BIGSERIAL PRIMARY KEY,
  message_id    BIGINT        NOT NULL,
  doc_name      VARCHAR(255)  NOT NULL DEFAULT '',
  chunk_content TEXT,
  score         DOUBLE PRECISION,
  create_time   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_chat_message_citation_message_id ON chat_message_citation (message_id);

COMMENT ON TABLE  chat_message_citation             IS 'AI 对话消息引用的知识片段';
COMMENT ON COLUMN chat_message_citation.id          IS '主键(自增)';
COMMENT ON COLUMN chat_message_citation.message_id  IS '关联助手消息ID(chat_message.id)';
COMMENT ON COLUMN chat_message_citation.doc_name    IS '来源文档名';
COMMENT ON COLUMN chat_message_citation.chunk_content IS '命中的知识片段内容';
COMMENT ON COLUMN chat_message_citation.score       IS '相似度分数';
COMMENT ON COLUMN chat_message_citation.create_time IS '创建时间';
