import { Routes as RouterRoutes, Route } from 'react-router';
import {
  Welcome,
  Login,
  Register,
  RecoverLostPassword,
  SecretMain,
  Profile,
  NotFound,
  Trip,
} from 'pages';
import { ProtectedRoute } from './ProtectedRoute';

export const Routes: React.FC = () => (
  <RouterRoutes>
    <Route path="/" element={<Welcome />} />

    <Route path="/login" element={<Login />} />

    <Route path="/register" element={<Register />} />

    <Route path="/recover-lost-password" element={<RecoverLostPassword />} />

    <Route element={<ProtectedRoute />}>
      <Route path="/main" element={<SecretMain />} />
      <Route path="/profile" element={<Profile />} />
    </Route>

    <Route path="/trip" element={<Trip />} />

    <Route path="*" element={<NotFound />} />
  </RouterRoutes>
);
