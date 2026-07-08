import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { BookListPage } from './pages/BookListPage'
import { BookDetailPage } from './pages/BookDetailPage'
import { Layout } from './components/Layout'
import { LoginPage } from './pages/LoginPage'
import { UserListPage } from './pages/UserListPage'
import { UserCreatePage } from './pages/UserCreatePage'
import { UserEditPage } from './pages/UserEditPage'
import { LoanBookListPage } from './pages/LoanBookListPage'
import { BookCreatePage } from './pages/BookCreatePage'
import { BookEditPage } from './pages/BookEditPage'


function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route path="/login" element={<LoginPage />} />
          <Route index element={<Navigate to="/books" replace/>} />
          <Route path="/books" element={<BookListPage />} />
          <Route path="/books/:id/edit" element={<BookEditPage />} />
          <Route path="/books/:id" element={<BookDetailPage />} />
          <Route path="/books/new" element={<BookCreatePage/>} />
          <Route path="/loans/active" element={<LoanBookListPage />} />
          <Route path="/users" element={<UserListPage/>} />
          <Route path="/users/new" element={<UserCreatePage/>} />
          <Route path='/users/:id/edit' element={<UserEditPage/>} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
