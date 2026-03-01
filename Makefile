# Makefile for Android Project

PACKAGE_NAME = com.example.sie_android_app
MAIN_ACTIVITY = .MainActivity

# Allow specifying a device
ADB_FLAGS = ${if ${DEVICE}, -s ${DEVICE},}

.PHONY: devices build install run clean log all help docs lint test coverage serve-docs

help:
	@echo "Available commands:"
	@echo "  make devices        - List available devices"
	@echo "  make build          - Build the Debug APK (assembleDebug)"
	@echo "  make install        - Build and Install the Debug APK (Auto-selects device)"
	@echo "  make run            - Launch the App on connected device (Auto-selects device)"
	@echo "  make all            - Build, Install, and Launch the App (Auto-selects device)"
	@echo "  make clean          - Clean the project"
	@echo "  make log            - Show Logcat for the app"
	@echo "  make docs           - Generate documentation site"
	@echo "  make serve-docs     - Serve documentation locally"
	@echo "  make lint           - Lint markdown files"
	@echo "  make test           - Run unit tests"
	@echo "  make coverage       - Run tests with coverage report"
	@echo ""
	@echo "Usage:"
	@echo "  make all                    (Auto-detects device or asks for selection)"
	@echo "  make all DEVICE=emulator-5554 (Target specific device)"


devices:
	adb devices

build:
	./gradlew assembleDebug

# Documentation / 文档生成
docs:
	@echo "Setting up virtual environment and generating documentation..."
	@python3 -m venv .venv
	@. .venv/bin/activate && pip install mkdocs mkdocs-material
	@echo "Preparing docs directory..."
	@cp README.md docs/index.md
	@cp CHANGELOG.md docs/CHANGELOG.md
	@cp LICENSE docs/LICENSE
	@. .venv/bin/activate && mkdocs build
	@echo "Documentation generated in site/ directory. Open site/index.html to view."

# Serve Documentation locally / 本地预览文档
serve-docs:
	@echo "Serving documentation..."
	@python3 -m venv .venv
	@. .venv/bin/activate && pip install mkdocs mkdocs-material
	@cp README.md docs/index.md
	@cp CHANGELOG.md docs/CHANGELOG.md
	@cp LICENSE docs/LICENSE
	@. .venv/bin/activate && mkdocs serve

# Linting / 代码规范检查
lint:
	@echo "Linting markdown files..."
	@if command -v markdownlint >/dev/null 2>&1; then \
		markdownlint README.md CHANGELOG.md; \
	else \
		echo "markdownlint not found. Please install it: npm install -g markdownlint-cli"; \
		exit 1; \
	fi

# Testing / 测试
test:
	./gradlew testDebugUnitTest

# Coverage / 覆盖率
coverage:
	./gradlew testDebugUnitTest
	@echo "Note: Ensure Jacoco is configured for detailed coverage reports."

clean:
	./gradlew clean
	rm -rf site .venv docs/index.md docs/CHANGELOG.md docs/LICENSE

# If DEVICE is defined, perform actions directly
ifdef DEVICE

install: build
	adb $(ADB_FLAGS) install -r app/build/outputs/apk/debug/app-debug.apk

run:
	adb $(ADB_FLAGS) shell am start -n $(PACKAGE_NAME)/$(MAIN_ACTIVITY)

log:
	adb $(ADB_FLAGS) logcat | grep $(PACKAGE_NAME)

all: install run

else
# If DEVICE is NOT defined, run the smart script to select device
# The script will recursively call make with DEVICE set

install:
	@./scripts/smart_run.sh install

run:
	@./scripts/smart_run.sh run

log:
	@./scripts/smart_run.sh log

all:
	@./scripts/smart_run.sh all

endif

