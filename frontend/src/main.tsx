import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import {BrowserRouter, Routes, Route} from 'react-router'
import Home from './components/Home.tsx'
import Login from './components/Login.tsx'
import { AuthProvider } from './context/AuthContext.tsx'
import ShowInfo from './components/ShowInfo.tsx'

// createRoot(document.getElementById('root')!).render(
//   <StrictMode>
//     <App />
//   </StrictMode>,
// )


createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AuthProvider>
      <BrowserRouter>
          <Routes>
            <Route path="/" element={<Home/>}></Route>
            <Route path="/login" element={<Login/>}></Route>
            <Route path="/info" element={<ShowInfo/>}></Route>
            <Route path="/home" element={<Home/>}></Route>
          </Routes>
        </BrowserRouter>
    </AuthProvider>
  </StrictMode>,
)