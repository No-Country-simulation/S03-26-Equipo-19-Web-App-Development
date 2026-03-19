import { useState } from 'react';
import { AuthContext } from './AuthContext';
import type { User } from './AuthContext';

const MOCK_USER: User = {
  id: '1',
  name: 'Augusto Zanetta',
  email: 'augusto@crm.com',
  role: 'admin',
};

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState<User | null>(MOCK_USER);
  const login = (user: User) => setUser(user);
  const logout = () => setUser(null);

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};