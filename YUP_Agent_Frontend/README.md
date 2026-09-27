# YUP Agent Frontend

基于 Vue 3、Vite、Vue Router 和 Axios 的智能应用工作台。

## 功能

- 应用主页：切换 AI 旅游助手与 AI 超级智能体。
- AI 旅游助手：通过 SSE 调用 `/ai/love_app/chat/sse`。
- AI 超级智能体：通过 SSE 调用 `/ai/manus/chat`。
- 每个聊天页面自动生成独立 Chat ID，并在当前浏览器会话中保留。
- 支持流式输出、停止生成、新建会话、复制 Chat ID、快捷提示词和移动端布局。

## 环境配置

开发环境使用 `.env.development`，直接请求本地 SpringBoot 服务：

```dotenv
VITE_API_BASE_URL=http://localhost:8123/api
```

此时需要后端允许前端开发地址跨域访问。

生产环境使用 `.env.production`，请求相对路径：

```dotenv
VITE_API_BASE_URL=/api
```

生产部署时需要由 Nginx、IIS 或其他网关将 `/api` 反向代理到 SpringBoot 服务。

## 本地运行

在 Windows PowerShell 中执行：

```powershell
npm install
npm run dev
```

默认访问地址：`http://localhost:5173`

Vite 开发配置中的 `/api` 代理目标为 `http://127.0.0.1:8123`。如果临时将开发环境地址改为 `/api`，开发服务器会使用该代理。

## 构建

```powershell
npm run build
npm run preview
```

生产构建文件生成在 `dist` 目录。