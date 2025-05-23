import { screen } from '@testing-library/react';
import { userEvent } from '@testing-library/user-event';
import { MemoryRouter } from 'react-router';
import { UserState } from 'contexts';
import { UserContext } from 'contexts/UserContext/User.context';
import { initialUserState } from 'contexts/UserContext/UserContext.constants';
import { Top } from './Top';
import { renderWithClient } from '../../tests';

const loggedUserState: UserState = {
  apiKey: 'test-api-key',
  user: {
    login: 'user-login',
    email: 'email@address.pl',
    createdOn: '2020-10-09T09:57:17.995288Z',
  },
  loggedIn: true,
};

const dispatch = vi.fn();
const mockMutate = vi.fn();

vi.mock('api/apiComponents', () => ({
  usePostUserLogout: () => ({
    mutateAsync: mockMutate,
    isSuccess: false,
  }),
}));

beforeEach(() => {
  vi.clearAllMocks();
});

test('renders brand name', () => {
  renderWithClient(
    <MemoryRouter initialEntries={['']}>
      <UserContext.Provider value={{ state: initialUserState, dispatch }}>
        <Top />
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(screen.getByText('Directdemo')).toBeInTheDocument();
});

test('renders nav bar unlogged user', () => {
  renderWithClient(
    <MemoryRouter initialEntries={['/main']}>
      <UserContext.Provider value={{ state: initialUserState, dispatch }}>
        <Top />
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(screen.getByText('Welcome')).toBeInTheDocument();
  expect(screen.getByText('Home')).toBeInTheDocument();
  expect(screen.getByText('Login')).toBeInTheDocument();
  expect(screen.getByText('Register')).toBeInTheDocument();
});

test('renders nav bar for logged user', () => {
  renderWithClient(
    <MemoryRouter initialEntries={['/main']}>
      <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
        <Top />
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(screen.getByText('Welcome')).toBeInTheDocument();
  expect(screen.getByText('Home')).toBeInTheDocument();
  expect(screen.getByText('user-login')).toBeInTheDocument();
  expect(screen.getByText('Logout')).toBeInTheDocument();
});

test('handles logout logged user', async () => {
  renderWithClient(
    <MemoryRouter initialEntries={['/main']}>
      <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
        <Top />
      </UserContext.Provider>
    </MemoryRouter>
  );
  await userEvent.click(screen.getByText(/logout/i));
  expect(mockMutate).toHaveBeenCalledTimes(1);
  expect(mockMutate).toHaveBeenCalledWith({ body: { apiKey: 'test-api-key' } });
});
