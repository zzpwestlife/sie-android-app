#!/bin/bash
# Performance Check Script for Modern Gradient UI
# Usage: ./scripts/performance-check.sh

set -e

echo "🔍 Modern Gradient UI - Performance Check"
echo "=========================================="
echo ""

# Check if device is connected
if ! adb devices | grep -q "device$"; then
    echo "❌ Error: No Android device connected"
    echo "   Please connect a device via USB or start an emulator"
    exit 1
fi

echo "✅ Device connected"
echo ""

# Get package name
PACKAGE_NAME="com.example.sie"

# Check if app is installed
if ! adb shell pm list packages | grep -q "$PACKAGE_NAME"; then
    echo "❌ Error: App not installed"
    echo "   Please install the app first: ./gradlew installDebug"
    exit 1
fi

echo "✅ App installed: $PACKAGE_NAME"
echo ""

# 1. Check Overdraw
echo "📊 Test 1: Checking Overdraw"
echo "----------------------------"
adb shell setprop debug.hwui.overdraw show
echo "✅ Overdraw visualization enabled"
echo "   Please check device screen:"
echo "   - Blue/Green = Good (1-2x overdraw)"
echo "   - Pink/Red = Bad (3-4x overdraw)"
echo ""
read -p "Press Enter to continue..."

# 2. Enable GPU Rendering Profile
echo ""
echo "📊 Test 2: Enabling GPU Rendering Profile"
echo "-------------------------------------------"
adb shell setprop debug.hwui.profile visual_bars
echo "✅ GPU rendering bars enabled"
echo "   Please check device screen:"
echo "   - Green bars below 16ms line = 60 FPS"
echo "   - Orange/Red bars above 16ms = Frame drops"
echo ""
read -p "Press Enter to continue..."

# 3. Check Memory Usage
echo ""
echo "📊 Test 3: Memory Usage Check"
echo "------------------------------"
echo "Starting app..."
adb shell am start -n "$PACKAGE_NAME/.MainActivity"
sleep 3

echo "Collecting memory stats..."
MEMINFO=$(adb shell dumpsys meminfo "$PACKAGE_NAME" | grep "TOTAL")
echo "$MEMINFO"

TOTAL_MB=$(echo "$MEMINFO" | awk '{print $2}' | awk '{printf "%.1f", $1/1024}')
echo ""
echo "📈 Total Memory Usage: ${TOTAL_MB} MB"

if (( $(echo "$TOTAL_MB < 150" | bc -l) )); then
    echo "✅ Memory usage is within target (< 150 MB)"
else
    echo "⚠️  Memory usage exceeds target (< 150 MB)"
fi
echo ""

# 4. FPS Measurement (requires Android 7.0+)
echo "📊 Test 4: Frame Rate Measurement"
echo "----------------------------------"
echo "Please navigate through the app for ~10 seconds"
echo "Testing will start in 3 seconds..."
sleep 3

adb shell dumpsys gfxinfo "$PACKAGE_NAME" reset > /dev/null
echo "Recording frame data..."
sleep 10

FPS_DATA=$(adb shell dumpsys gfxinfo "$PACKAGE_NAME")
echo "$FPS_DATA" | grep -A 5 "Total frames rendered"
echo ""

# Extract jank percentage
JANKY=$(echo "$FPS_DATA" | grep "Janky frames:" | awk '{print $3}' | sed 's/[()%]//g')
if [ ! -z "$JANKY" ]; then
    echo "📈 Janky Frames: ${JANKY}%"
    if (( $(echo "$JANKY < 5" | bc -l) )); then
        echo "✅ Frame rate is excellent (< 5% jank)"
    elif (( $(echo "$JANKY < 10" | bc -l) )); then
        echo "⚠️  Frame rate is acceptable (5-10% jank)"
    else
        echo "❌ Frame rate needs improvement (> 10% jank)"
    fi
fi
echo ""

# 5. Cold Start Time (approximation)
echo "📊 Test 5: Cold Start Time (Approximate)"
echo "-----------------------------------------"
echo "Force stopping app..."
adb shell am force-stop "$PACKAGE_NAME"
sleep 1

echo "Starting app and measuring..."
START_TIME=$(date +%s%N)
adb shell am start -W -n "$PACKAGE_NAME/.MainActivity" | grep "TotalTime" | awk '{print $2}'
END_TIME=$(date +%s%N)

ELAPSED=$((($END_TIME - $START_TIME) / 1000000))
echo "⏱️  Cold Start Time: ${ELAPSED}ms"

if [ $ELAPSED -lt 2000 ]; then
    echo "✅ Cold start time is excellent (< 2000ms)"
elif [ $ELAPSED -lt 3000 ]; then
    echo "⚠️  Cold start time is acceptable (2000-3000ms)"
else
    echo "❌ Cold start time needs improvement (> 3000ms)"
fi
echo ""

# Cleanup
echo "🧹 Cleanup"
echo "----------"
adb shell setprop debug.hwui.overdraw false
adb shell setprop debug.hwui.profile false
echo "✅ Disabled debug overlays"
echo ""

# Summary
echo "✅ Performance Check Complete"
echo "============================="
echo ""
echo "📋 Summary:"
echo "   1. Overdraw: Check device screen (disabled now)"
echo "   2. GPU Rendering: Check device screen (disabled now)"
echo "   3. Memory Usage: ${TOTAL_MB} MB"
if [ ! -z "$JANKY" ]; then
    echo "   4. Frame Rate: ${JANKY}% jank"
fi
echo "   5. Cold Start: ${ELAPSED}ms"
echo ""
echo "📖 For detailed profiling, use Android Studio Profiler"
echo "   https://developer.android.com/studio/profile"
