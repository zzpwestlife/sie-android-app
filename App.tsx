// INPUT: react-navigation
// OUTPUT: 应用根组件
// POS: React Native 应用入口

import React from 'react';
import 'react-native-gesture-handler';

import {AppNavigator} from '@/navigation';

const App: React.FC = () => {
  return <AppNavigator />;
};

export default App;
