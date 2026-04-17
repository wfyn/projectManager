# 北化服务器

## VPN / 堡垒机
| 项目 | 信息 |
|------|------|
| 地址 | https://202.4.128.50:1443/prx/000/http/localhost/login/index.html |
| 账号 | `menhunew4` |

## 制作服务器（新教师个人主页）
| 项目 | 信息 |
|------|------|
| IP | `121.195.148.203` |
| 账号 | `liusm` |
| 密码 | `js05!QAZ#Buct` |

## 数据库服务器（新教师个人主页）
| 项目 | 信息 |
|------|------|
| IP | `121.195.148.220` |
| 账号 | `liusm` |
| 密码 | `jm03!QAZ#Buct` |
| 数据库账号 | `root / Sudy.web123` |

## 前端服务器
| 项目 | 信息 |
|------|------|
| IP | `121.195.148.229` |
| 账号 | `yough` |
| 密码 | `wz03!QAZ#Buct` |

## AI 门户
| IP | 账号 | 密码 |
|----|------|------|
| `121.195.148.195` | root | `ai08!QAZ#Buct` |
| `121.195.132.9` | yough | `wz01!QAZ#Buct` / `js05!QAZ#Buct` |

## 运维命令

**删除记录**
```bash
curl --location --request POST 'http://121.195.149.8:31144/v1/mgr/article/del' \
--header 'Content-Type: application/json' \
--data '{
    "resType": "teacherHome"
}'
```
