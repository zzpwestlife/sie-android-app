// INPUT: react-navigation/stack, react-navigation/native, screens
// OUTPUT: 类型安全的导航容器
// POS: 应用根导航入口

import {NavigationContainer} from '@react-navigation/native';
import {createStackNavigator} from '@react-navigation/stack';
import React from 'react';

import {HomeScreen} from '@/screens';

import type {RootStackParamList} from './types';

const Stack = createStackNavigator<RootStackParamList>();

export const AppNavigator: React.FC = () => {
  return (
    <NavigationContainer>
      <Stack.Navigator initialRouteName="Home">
        <Stack.Screen
          name="Home"
          component={HomeScreen}
          options={{title: 'Home'}}
        />
      </Stack.Navigator>
    </NavigationContainer>
  );
};
