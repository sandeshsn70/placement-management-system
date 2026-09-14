import { Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'
import { StudentProfileProvider } from './context/StudentProfileContext'
import ProtectedRoute from './components/ProtectedRoute'
import Layout from './components/Layout'

import Login from './pages/Login'
import Register from './pages/Register'
import Dashboard from './pages/Dashboard'
import Approvals from './pages/Approvals'
import Departments from './pages/Departments'
import Students from './pages/Students'
import Companies from './pages/Companies'
import Jobs from './pages/Jobs'
import PlacementDrives from './pages/PlacementDrives'
import Applications from './pages/Applications'
import Interviews from './pages/Interviews'
import Offers from './pages/Offers'
import Users from './pages/Users'

const STAFF = ['ADMIN', 'PLACEMENT_OFFICER']

function AuthGate({ children }) {
  const { user } = useAuth()
  return user ? <Navigate to="/" replace /> : children
}

export default function App() {
  return (
    <AuthProvider>
      <StudentProfileProvider>
        <Routes>
          <Route path="/login" element={<AuthGate><Login /></AuthGate>} />
          <Route path="/register" element={<AuthGate><Register /></AuthGate>} />

          <Route
            path="/"
            element={
              <ProtectedRoute>
                <Layout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Dashboard />} />
            <Route path="approvals" element={<ProtectedRoute roles={STAFF}><Approvals /></ProtectedRoute>} />
            <Route path="departments" element={<ProtectedRoute roles={STAFF}><Departments /></ProtectedRoute>} />
            <Route path="students" element={<ProtectedRoute roles={STAFF}><Students /></ProtectedRoute>} />
            <Route path="companies" element={<Companies />} />
            <Route path="jobs" element={<Jobs />} />
            <Route path="drives" element={<ProtectedRoute roles={STAFF}><PlacementDrives /></ProtectedRoute>} />
            <Route path="applications" element={<Applications />} />
            <Route path="interviews" element={<Interviews />} />
            <Route path="offers" element={<Offers />} />
            <Route path="users" element={<ProtectedRoute roles={['ADMIN']}><Users /></ProtectedRoute>} />
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </StudentProfileProvider>
    </AuthProvider>
  )
}
