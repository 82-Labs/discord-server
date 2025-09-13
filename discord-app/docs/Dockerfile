FROM node:18-alpine

RUN npm install -g @redocly/cli

WORKDIR /docs

CMD ["redocly", "build-docs", "openapi.json", "-o", "api-docs.html"]