#!/bin/bash

# smart_run.sh - Auto-detect Android devices and run make target

TARGET=$1

if [ -z "$TARGET" ]; then
    echo "Usage: $0 <make_target>"
    exit 1
fi

# Get list of connected devices (excluding "List of devices attached" and empty lines)
# grep -w "device" filters out "offline", "unauthorized", etc.
DEVICES_LIST=$(adb devices | grep -w "device" | awk '{print $1}')
COUNT=$(echo "$DEVICES_LIST" | wc -w | tr -d '[:space:]')

if [ "$COUNT" -eq 0 ]; then
    echo "❌ Error: No connected Android devices found."
    echo "   Please connect a device via USB or start an Emulator."
    echo "   Tip: Use 'make devices' to check connection status."
    exit 1
fi

if [ "$COUNT" -eq 1 ]; then
    # Only one device, use it automatically
    SELECTED_DEVICE=$(echo "$DEVICES_LIST" | tr -d '[:space:]')
    echo "✅ Auto-detected device: $SELECTED_DEVICE"
else
    # Multiple devices, ask user to select
    echo "📱 Multiple devices found:"
    
    # Read into array
    DEVICE_ARRAY=($DEVICES_LIST)
    
    i=1
    for DEV in "${DEVICE_ARRAY[@]}"; do
        # Try to get model name for better identification
        MODEL=$(adb -s $DEV shell getprop ro.product.model 2>/dev/null | tr -d '\r')
        if [ -z "$MODEL" ]; then
            MODEL="Unknown Device"
        fi
        echo "  $i) $DEV ($MODEL)"
        ((i++))
    done
    
    echo -n "👉 Select device (1-$COUNT): "
    read CHOICE
    
    # Validate input
    if [[ ! "$CHOICE" =~ ^[0-9]+$ ]] || [ "$CHOICE" -lt 1 ] || [ "$CHOICE" -gt "$COUNT" ]; then
        echo "❌ Invalid selection."
        exit 1
    fi
    
    INDEX=$((CHOICE-1))
    SELECTED_DEVICE=${DEVICE_ARRAY[$INDEX]}
    echo "✅ Selected: $SELECTED_DEVICE"
fi

echo "🚀 Running: make $TARGET DEVICE=$SELECTED_DEVICE"
echo "---------------------------------------------------"

# Execute the make command with the selected device
make "$TARGET" DEVICE="$SELECTED_DEVICE"
