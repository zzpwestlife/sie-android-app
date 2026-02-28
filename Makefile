# Makefile for Android Project

PACKAGE_NAME = com.example.sie_android_app
MAIN_ACTIVITY = .MainActivity

# Allow specifying a device
ADB_FLAGS = ${if ${DEVICE}, -s ${DEVICE},}

.PHONY: devices build install run clean log all help

help:
	@echo "Available commands:"
	@echo "  make devices        - List available devices"
	@echo "  make build          - Build the Debug APK (assembleDebug)"
	@echo "  make install        - Build and Install the Debug APK (Auto-selects device)"
	@echo "  make run            - Launch the App on connected device (Auto-selects device)"
	@echo "  make all            - Build, Install, and Launch the App (Auto-selects device)"
	@echo "  make clean          - Clean the project"
	@echo "  make log            - Show Logcat for the app"
	@echo ""
	@echo "Usage:"
	@echo "  make all                    (Auto-detects device or asks for selection)"
	@echo "  make all DEVICE=emulator-5554 (Target specific device)"


devices:
	adb devices

build:
	./gradlew assembleDebug

clean:
	./gradlew clean

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
