import { Route, Routes } from 'react-router-dom'
import Layout from './components/Layout'
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import CompanyFilesPage from './pages/CompanyFilesPage'
import DocumentPage from './pages/DocumentPage'
import EvidencePage from './pages/EvidencePage'
import BidPage from './pages/BidPage'

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<LoginPage />} />
      <Route element={<Layout />}>
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/company-files" element={<CompanyFilesPage />} />
        <Route path="/documents" element={<DocumentPage />} />
        <Route path="/evidence" element={<EvidencePage />} />
        <Route path="/bids" element={<BidPage />} />
      </Route>
    </Routes>
  )
}
