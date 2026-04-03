#!/bin/bash

# Docker Compose 管理服务脚本
# 支持操作: up/down/restart/logs/status

# 服务定义（与 docker-compose.yaml 保持一致）
ALL_SERVICES="attachment-parsing security-tok-api webplus-push-badger security-service nats"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 显示帮助信息
function show_help() {
    echo "用法: $0 {composeup|composedown|restart|logs|status} [服务名]"
    echo ""
    echo "操作命令:"
    echo "  composeup          启动所有服务"
    echo "  composeup <服务名> 启动指定服务"
    echo "  composedown        停止所有服务"
    echo "  restart            重启所有服务"
    echo "  restart <服务名>   重启指定服务"
    echo "  logs               查看所有服务日志"
    echo "  logs <服务名>      查看指定服务日志"
    echo "  status             查看服务运行状态"
    echo ""
    echo "可用服务: $ALL_SERVICES"
    echo ""
    echo "示例:"
    echo "  $0 composeup                    # 启动所有服务"
    echo "  $0 composeup security-service   # 仅启动 security-service"
    echo "  $0 logs webplus-push-badger     # 查看推送服务日志"
    exit 1
}

# 检查 docker-compose 文件是否存在
function check_compose_file() {
    if [ ! -f "docker-compose.yaml" ]; then
        echo -e "${RED}错误: docker-compose.yaml 文件不存在${NC}"
        exit 1
    fi
}

# 检查服务名是否有效
function check_service() {
    local service=$1
    if [[ ! " $ALL_SERVICES " =~ " $service " ]]; then
        echo -e "${RED}错误: 未知服务 '$service'${NC}"
        echo -e "可用服务: $ALL_SERVICES"
        exit 1
    fi
}

# 主逻辑
case "$1" in
    composeup)
        check_compose_file
        if [ -n "$2" ]; then
            # 启动指定服务
            check_service $2
            echo -e "${GREEN}启动服务: $2${NC}"
            docker-compose -f docker-compose.yaml up -d $2
        else
            # 启动所有服务
            echo -e "${GREEN}正在启动所有服务...${NC}"
            docker-compose -f docker-compose.yaml up -d
        fi
        ;;

    composedown)
        check_compose_file
        echo -e "${YELLOW}正在停止所有服务...${NC}"
        docker-compose -f docker-compose.yaml down
        ;;

    restart)
        check_compose_file
        if [ -n "$2" ]; then
            # 重启指定服务
            check_service $2
            echo -e "${GREEN}重启服务: $2${NC}"
            docker-compose -f docker-compose.yaml restart $2
        else
            # 重启所有服务
            echo -e "${YELLOW}正在重启所有服务...${NC}"
            docker-compose -f docker-compose.yaml restart
        fi
        ;;

    logs)
        check_compose_file
        if [ -n "$2" ]; then
            # 查看指定服务日志
            check_service $2
            echo -e "${GREEN}查看服务日志: $2${NC}"
            docker-compose -f docker-compose.yaml logs -f --tail=100 $2
        else
            # 查看所有服务日志
            echo -e "${YELLOW}查看所有服务日志...${NC}"
            docker-compose -f docker-compose.yaml logs -f --tail=50
        fi
        ;;

    status|ps)
        check_compose_file
        echo -e "${GREEN}服务状态:${NC}"
        docker-compose -f docker-compose.yaml ps
        ;;

    *)
        show_help
        ;;
esac

# 显示最终状态
if [ "$1" == "composeup" ] || [ "$1" == "restart" ]; then
    echo ""
    echo -e "${GREEN}操作完成，当前服务状态:${NC}"
    docker-compose ps
fi
