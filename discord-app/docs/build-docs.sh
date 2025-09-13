#!/bin/bash

# OpenAPI JSON 생성
echo "📝 OpenAPI JSON을 생성합니다..."
cd ../..
./gradlew :discord-app:generateOpenApiDocs
cd discord-app/docs

# OpenAPI JSON이 있는지 확인
if [ ! -f "openapi.json" ]; then
    echo "❌ openapi.json 생성 실패"
    exit 1
fi

# Docker 빌드 및 실행
echo "🎨 HTML 문서를 생성합니다..."
docker build -t redocly-builder .
docker run --rm -v $(pwd):/docs redocly-builder

# HTML 파일을 정적 리소스 디렉토리로 복사
echo "📦 HTML 파일을 정적 리소스로 복사합니다..."
mkdir -p ../src/main/resources/static/docs
cp api-docs.html ../src/main/resources/static/docs/

# 임시 파일 정리
echo "🧹 임시 파일을 정리합니다..."
rm -f openapi.json api-docs.html

echo "✅ API 문서가 생성되었습니다: /docs/api-docs.html"