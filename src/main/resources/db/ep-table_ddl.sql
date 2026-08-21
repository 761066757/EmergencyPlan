-- =======================================================================================================================================
-- 切换到业务专属的 public schema
SET SCHEMA 'public';

-- =============================================
-- 第一步：创建通用更新时间触发器
-- =============================================
BEGIN;

-- 1. 创建触发器函数（通用，所有表复用）
CREATE OR REPLACE FUNCTION public.update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql VOLATILE;
COMMENT ON FUNCTION public.update_modified_column() IS '通用更新时间触发器函数：自动刷新update_time字段';

COMMIT;


-- =============================================
-- 第二步：创建 任务表
-- =============================================
BEGIN;

CREATE TABLE IF NOT EXISTS public.emergency_task (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
	station VARCHAR(32) NOT NULL,
	task_type VARCHAR(64) NOT NULL,
    task_code SMALLINT DEFAULT 0,
    task_name VARCHAR(128) NOT NULL,
    task_desc VARCHAR(255),
	is_skippable SMALLINT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT DEFAULT 0
);
-- 表/字段注释
COMMENT ON TABLE public.emergency_task IS '任务表';
COMMENT ON COLUMN public.emergency_task.id IS '主键ID';
COMMENT ON COLUMN public.emergency_task.station IS '车站';
COMMENT ON COLUMN public.emergency_task.task_type IS '任务类型';
COMMENT ON COLUMN public.emergency_task.task_code IS '任务编码';
COMMENT ON COLUMN public.emergency_task.task_name IS '任务名称';
COMMENT ON COLUMN public.emergency_task.task_desc IS '任务描述';
COMMENT ON COLUMN public.emergency_task.is_skippable IS '是否允许跳过：0-否，1-是';
COMMENT ON COLUMN public.emergency_task.create_time IS '创建时间';
COMMENT ON COLUMN public.emergency_task.update_time IS '更新时间';
COMMENT ON COLUMN public.emergency_task.is_deleted IS '删除标记：0-未删除，1-已删除';

-- 绑定通用触发器
CREATE TRIGGER update_emergency_task_modtime
BEFORE UPDATE ON public.emergency_task
FOR EACH ROW
EXECUTE FUNCTION public.update_modified_column();

COMMIT;


-- =============================================
-- 第三步：创建 预案表
-- =============================================
BEGIN;

CREATE TABLE IF NOT EXISTS public.emergency_plan (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
	station VARCHAR(32) NOT NULL,
    plan_name VARCHAR(128) NOT NULL,
    plan_type VARCHAR(64) DEFAULT 'emergency',
    plan_desc VARCHAR(255),
    plan_doc VARCHAR(255),
    plan_status SMALLINT DEFAULT 0,
    bpmn_xml TEXT,
    camera_ids VARCHAR(255),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT DEFAULT 0
);

-- 表/字段注释
COMMENT ON TABLE public.emergency_plan IS '预案表';
COMMENT ON COLUMN public.emergency_plan.id IS '主键ID';
COMMENT ON COLUMN public.emergency_plan.station IS '车站';
COMMENT ON COLUMN public.emergency_plan.plan_type IS '预案类型';
COMMENT ON COLUMN public.emergency_plan.plan_name IS '预案名称';
COMMENT ON COLUMN public.emergency_plan.plan_desc IS '预案描述';
COMMENT ON COLUMN public.emergency_plan.plan_doc IS '预案文档';
COMMENT ON COLUMN public.emergency_plan.plan_status IS '预案状态：0-未发布，1-已发布，2-执行中';
COMMENT ON COLUMN public.emergency_plan.bpmn_xml IS 'Flowable流程XML';
COMMENT ON COLUMN public.emergency_plan.camera_ids IS '关联摄像头ID';
COMMENT ON COLUMN public.emergency_plan.create_time IS '创建时间';
COMMENT ON COLUMN public.emergency_plan.update_time IS '更新时间';
COMMENT ON COLUMN public.emergency_plan.is_deleted IS '逻辑删除：0-未删，1-已删';

-- 绑定通用触发器
CREATE TRIGGER update_emergency_plan_modtime
BEFORE UPDATE ON public.emergency_plan
FOR EACH ROW
EXECUTE FUNCTION public.update_modified_column();

COMMIT;


-- =============================================
-- 第四步：创建 预案与任务关联表
-- =============================================
BEGIN;

CREATE TABLE IF NOT EXISTS public.emergency_plan_task_rel (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
	station VARCHAR(32) NOT NULL,
    plan_id VARCHAR(32) NOT NULL,
    task_type VARCHAR(64) NOT NULL,
    flow_node_id VARCHAR(64),
    flow_node_name VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT DEFAULT 0
);

-- 表/字段注释
COMMENT ON TABLE public.emergency_plan_task_rel IS '预案与任务关联表';
COMMENT ON COLUMN public.emergency_plan_task_rel.id IS '主键UUID';
COMMENT ON COLUMN public.emergency_plan_task_rel.station IS '车站';
COMMENT ON COLUMN public.emergency_plan_task_rel.plan_id IS '预案ID';
COMMENT ON COLUMN public.emergency_plan_task_rel.task_type IS '任务类型';
COMMENT ON COLUMN public.emergency_plan_task_rel.flow_node_id IS 'Flowable节点ID';
COMMENT ON COLUMN public.emergency_plan_task_rel.flow_node_name IS 'Flowable节点名称';
COMMENT ON COLUMN public.emergency_plan_task_rel.create_time IS '创建时间';
COMMENT ON COLUMN public.emergency_plan_task_rel.update_time IS '更新时间';
COMMENT ON COLUMN public.emergency_plan_task_rel.is_deleted IS '逻辑删除：0-未删，1-已删';

-- 索引
CREATE UNIQUE INDEX IF NOT EXISTS idx_plan_task ON public.emergency_plan_task_rel(plan_id, task_type) WHERE is_deleted = 0;
CREATE INDEX IF NOT EXISTS idx_plan_id ON public.emergency_plan_task_rel(plan_id) WHERE is_deleted = 0;
CREATE INDEX IF NOT EXISTS idx_task_type ON public.emergency_plan_task_rel(task_type) WHERE is_deleted = 0;

-- 绑定通用触发器
CREATE TRIGGER update_plan_task_rel_modtime
BEFORE UPDATE ON public.emergency_plan_task_rel
FOR EACH ROW
EXECUTE FUNCTION public.update_modified_column();

COMMIT;


-- =============================================
-- 第五步：创建 预案与流程关联表
-- =============================================
BEGIN;

CREATE TABLE IF NOT EXISTS public.emergency_plan_flow_rel (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
	station VARCHAR(32) NOT NULL,
    plan_id VARCHAR(32) NOT NULL,
	deploy_id VARCHAR(64),
    cur_proc_inst_id VARCHAR(64),
    current_task_id VARCHAR(255),
    current_task_name VARCHAR(255),
    flow_status SMALLINT DEFAULT 1,
	opt_name VARCHAR(32),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT DEFAULT 0
);

-- 表/字段注释
COMMENT ON TABLE public.emergency_plan_flow_rel IS '预案与流程关联表';
COMMENT ON COLUMN public.emergency_plan_flow_rel.id IS '主键';
COMMENT ON COLUMN public.emergency_plan_flow_rel.station IS '车站';
COMMENT ON COLUMN public.emergency_plan_flow_rel.plan_id IS '预案ID';
COMMENT ON COLUMN public.emergency_plan_flow_rel.deploy_id IS 'Flowable部署ID';
COMMENT ON COLUMN public.emergency_plan_flow_rel.cur_proc_inst_id IS 'Flowable流程实例ID（当前）';
COMMENT ON COLUMN public.emergency_plan_flow_rel.current_task_id IS '当前待办任务ID';
COMMENT ON COLUMN public.emergency_plan_flow_rel.current_task_name IS '当前待办任务名称';
COMMENT ON COLUMN public.emergency_plan_flow_rel.flow_status IS '流程状态：0-未运行 1-运行中 2-已结束 3-终止';
COMMENT ON COLUMN public.emergency_plan_flow_rel.opt_name IS '操作人';
COMMENT ON COLUMN public.emergency_plan_flow_rel.create_time IS '创建时间';
COMMENT ON COLUMN public.emergency_plan_flow_rel.update_time IS '更新时间';
COMMENT ON COLUMN public.emergency_plan_flow_rel.is_deleted IS '逻辑删除：0-未删，1-已删';

-- 绑定通用触发器
CREATE TRIGGER update_plan_flow_rel_modtime
BEFORE UPDATE ON public.emergency_plan_flow_rel
FOR EACH ROW
EXECUTE FUNCTION public.update_modified_column();

COMMIT;


-- =============================================
-- 第六步：创建 流程实例历史表
-- =============================================
BEGIN;

CREATE TABLE IF NOT EXISTS public.emergency_flow_inst_hi (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
	station VARCHAR(32) NOT NULL,
    plan_id VARCHAR(32) NOT NULL,
    plan_name VARCHAR(128) NOT NULL,
	plan_type VARCHAR(64) DEFAULT 'emergency',
	plan_doc VARCHAR(255),
	bpmn_xml TEXT,
	camera_ids VARCHAR(255),
	video_urls VARCHAR(255),
	deploy_id VARCHAR(64),
    hi_proc_inst_id VARCHAR(64),
	is_history SMALLINT DEFAULT 0,
	opt_name VARCHAR(32),
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT DEFAULT 0
);

-- 表/字段注释
COMMENT ON TABLE public.emergency_flow_inst_hi IS '流程实例历史表';
COMMENT ON COLUMN public.emergency_flow_inst_hi.id IS '主键';
COMMENT ON COLUMN public.emergency_flow_inst_hi.station IS '车站';
COMMENT ON COLUMN public.emergency_flow_inst_hi.plan_id IS '预案ID';
COMMENT ON COLUMN public.emergency_flow_inst_hi.plan_name IS '预案名称';
COMMENT ON COLUMN public.emergency_flow_inst_hi.plan_type IS '预案类型';
COMMENT ON COLUMN public.emergency_flow_inst_hi.plan_doc IS '预案文档';
COMMENT ON COLUMN public.emergency_flow_inst_hi.bpmn_xml IS 'Flowable流程XML';
COMMENT ON COLUMN public.emergency_flow_inst_hi.camera_ids IS '关联摄像头ID';
COMMENT ON COLUMN public.emergency_flow_inst_hi.video_urls IS '录像回放';
COMMENT ON COLUMN public.emergency_flow_inst_hi.deploy_id IS 'Flowable部署ID';
COMMENT ON COLUMN public.emergency_flow_inst_hi.hi_proc_inst_id IS 'Flowable流程实例ID（历史）';
COMMENT ON COLUMN public.emergency_flow_inst_hi.is_history IS '是否历史记录：0-否，1-是';
COMMENT ON COLUMN public.emergency_flow_inst_hi.opt_name IS '操作人';
COMMENT ON COLUMN public.emergency_flow_inst_hi.start_time IS '开始时间';
COMMENT ON COLUMN public.emergency_flow_inst_hi.end_time IS '结束时间';
COMMENT ON COLUMN public.emergency_flow_inst_hi.create_time IS '创建时间';
COMMENT ON COLUMN public.emergency_flow_inst_hi.update_time IS '更新时间';
COMMENT ON COLUMN public.emergency_flow_inst_hi.is_deleted IS '逻辑删除：0-未删，1-已删';

-- 绑定通用触发器
CREATE TRIGGER update_flow_inst_hi_modtime
BEFORE UPDATE ON public.emergency_flow_inst_hi
FOR EACH ROW
EXECUTE FUNCTION public.update_modified_column();

COMMIT;