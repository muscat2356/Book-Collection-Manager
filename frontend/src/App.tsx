import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { BookListPage } from './pages/BookListPage'
import { BookDetailPage } from './pages/BookDetailPage'
import { Layout } from './components/Layout'
import { UserListPage } from './pages/UserListPage'
import { UserCreatePage } from './pages/UserCreatePage'
import { UserEditPage } from './pages/UserEditPage'
import { LoanBookListPage } from './pages/LoanBookListPage'
import { BookCreatePage } from './pages/BookCreatePage'
import { BookEditPage } from './pages/BookEditPage'
import { AccessDeniedPage } from './pages/AccessDeniedPage'
import { ProtectedRoute } from './components/ProtectedRoute'
import { LoanProvider } from './loans/LoanContext'
import { CheckoutLayout } from './components/CheckoutLayout'
import { CheckoutBookDetailPage } from './pages/CheckoutBookDetailPage'
import { CheckoutBookListPage } from './pages/CheckoutBookListPage'
import { CheckoutConfirmPage } from './pages/CheckoutConfirmPage'
import { CheckoutUserSelectPage } from './pages/CheckoutUserSelectPage'


function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/access-denied" element={<AccessDeniedPage />} />
        <Route element={<ProtectedRoute />}>
          <Route element={<Layout />}>
            <Route index element={<Navigate to="/books" replace />} />
            <Route path="/books" element={<BookListPage />} />
            <Route path="/books/:id" element={<BookDetailPage />} />
            <Route path="/loans/active" element={<LoanBookListPage />} />
            <Route path="/users" element={<UserListPage />} />
            <Route path="/users/new" element={<UserCreatePage />} />
            <Route path="/users/:id/edit" element={<UserEditPage />} />
            <Route
              path="/loans/checkout"
              element={
                <LoanProvider>
                  <CheckoutLayout />
                </LoanProvider>
              }
            >
              <Route index element={<CheckoutUserSelectPage />} />
              <Route path="books" element={<CheckoutBookListPage />} />
              <Route path="books/:id" element={<CheckoutBookDetailPage />} />
              <Route path="confirm" element={<CheckoutConfirmPage />} />
            </Route>
            <Route element={<ProtectedRoute requireAdmin />}>
              <Route path="/books/new" element={<BookCreatePage />} />
              <Route path="/books/:id/edit" element={<BookEditPage />} />
            </Route>
          </Route>
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
