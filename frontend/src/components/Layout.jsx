import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const NAV_ITEMS = [
  { to: '/', label: 'Dashboard', roles: ['ADMIN', 'PLACEMENT_OFFICER', 'STUDENT'] },
  { to: '/approvals', label: 'Pending Approvals', roles: ['ADMIN', 'PLACEMENT_OFFICER'] },
  { to: '/departments', label: 'Departments', roles: ['ADMIN', 'PLACEMENT_OFFICER'] },
  { to: '/students', label: 'Students', roles: ['ADMIN', 'PLACEMENT_OFFICER'] },
  { to: '/companies', label: 'Companies', roles: ['ADMIN', 'PLACEMENT_OFFICER', 'STUDENT'] },
  { to: '/jobs', label: 'Job Postings', roles: ['ADMIN', 'PLACEMENT_OFFICER', 'STUDENT'] },
  { to: '/drives', label: 'Placement Drives', roles: ['ADMIN', 'PLACEMENT_OFFICER'] },
  { to: '/applications', label: 'Applications', roles: ['ADMIN', 'PLACEMENT_OFFICER', 'STUDENT'] },
  { to: '/interviews', label: 'Interviews', roles: ['ADMIN', 'PLACEMENT_OFFICER', 'STUDENT'] },
  { to: '/offers', label: 'Offers', roles: ['ADMIN', 'PLACEMENT_OFFICER', 'STUDENT'] },
  { to: '/users', label: 'User Accounts', roles: ['ADMIN'] }
]

const ROLE_LABEL = {
  ADMIN: 'Administrator',
  PLACEMENT_OFFICER: 'Placement Officer',
  STUDENT: 'Student'
}

export default function Layout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const items = NAV_ITEMS.filter((item) => item.roles.includes(user.role))

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="sidebar-mark">
          Placement Cell
          <small>Campus Recruitment Register</small>
        </div>
        <nav className="sidebar-nav">
          {items.map((item, i) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/'}
              className={({ isActive }) => 'sidebar-link' + (isActive ? ' active' : '')}
            >
              <span className="idx">{String(i + 1).padStart(2, '0')}</span>
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-foot">
          <div className="who">{user.username}</div>
          <div className="role-tag">{ROLE_LABEL[user.role] || user.role}</div>
          <button onClick={handleLogout}>Sign out</button>
        </div>
      </aside>
      <main className="main">
        <Outlet />
      </main>
    </div>
  )
}
