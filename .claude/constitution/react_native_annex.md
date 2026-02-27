# React Native Development Standards

## Core Principles
- **Functional Components**: Use functional components with Hooks. Avoid Class components.
- **TypeScript**: Use TypeScript for all new components and logic.
- **Type Safety**: Ensure type safety using Interfaces or Types. Avoid `any`.
- **Componentization**: Break down UI into small, reusable components.

## Styling
- **StyleSheet**: Use `StyleSheet.create` for styles. Avoid inline styles for performance.
- **Flexbox**: Use Flexbox for layout.
- **Theming**: Support Dark Mode/Light Mode using `useColorScheme`.

## State Management
- **Local State**: Use `React.useState` for local state.
- **Global State**: Use Context API or established libraries (Zustand/Redux) if needed.

## Performance
- **Memoization**: Memoize callbacks with `useCallback` and expensive calculations with `useMemo`.
- **Lists**: Use `FlatList` instead of `ScrollView` for long lists.
- **Images**: Use optimized image components.

## Testing
- **Jest**: Use Jest for unit testing.
- **React Native Testing Library**: Use for component testing.
