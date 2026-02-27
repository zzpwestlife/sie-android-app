// INPUT: React, react-native, useCounterStore
// OUTPUT: 主屏幕组件
// POS: 应用主界面

import React from 'react';
import {Button, StyleSheet, Text, View} from 'react-native';

import {useCounterStore} from '@/stores';

export const HomeScreen: React.FC = () => {
  const {count, increment, decrement, reset} = useCounterStore();

  return (
    <View style={styles.container}>
      <Text style={styles.title}>React Native App</Text>
      <Text style={styles.count}>Count: {count}</Text>
      <View style={styles.buttonGroup}>
        <Button title="+" onPress={increment} />
        <Button title="-" onPress={decrement} />
        <Button title="Reset" onPress={reset} />
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 20,
  },
  count: {
    fontSize: 48,
    marginBottom: 30,
  },
  buttonGroup: {
    flexDirection: 'row',
    gap: 10,
  },
});
