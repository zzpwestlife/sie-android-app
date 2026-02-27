// INPUT: zustand
// OUTPUT: 计数器状态管理 Hook
// POS: 全局状态示例

import {create} from 'zustand';

interface CounterState {
  count: number;
  increment: () => void;
  decrement: () => void;
  reset: () => void;
}

export const useCounterStore = create<CounterState>(set => ({
  count: 0,
  increment: () => set(state => ({count: state.count + 1})),
  decrement: () => set(state => ({count: state.count - 1})),
  reset: () => set({count: 0}),
}));
