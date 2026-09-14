FROM node:22-alpine AS build
WORKDIR /workspace
COPY package.json pnpm-lock.yaml ./
COPY patches ./patches
RUN corepack enable && pnpm install --frozen-lockfile
COPY client ./client
COPY server ./server
COPY shared ./shared
COPY vite.config.ts tsconfig.json tsconfig.node.json components.json ./
ARG VITE_API_BASE_URL=/api/v1
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
RUN pnpm build

FROM nginx:1.27-alpine
COPY client/deployment/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/dist/public /usr/share/nginx/html
EXPOSE 80
