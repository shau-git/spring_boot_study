import {useEffect} from 'react'
import { useAuth } from '../context/AuthContext'
import { useNavigate , useLocation} from 'react-router'
import backend_domain from "../backend_domain";

const Login = () => {
    const {isAuthenticated} = useAuth()
    const navigate = useNavigate()
    const location = useLocation()
    const fromLocation = (location.state)?.from?.pathname || "/"
    console.log(location.state, " ----------------- is useLocation() Login.tsx   line 10")

    useEffect(() => {
        if(isAuthenticated) {
            navigate("fromLocation", {replace: true}) // if user is authenticated user's current url , will go back to where they original is, if not authenticated will continue stay at login page
        }
    }, [isAuthenticated, fromLocation, navigate])

    const handleLogin = (provider: string) => {
        console.log("You login")
        const url = `${backend_domain}/oauth2/authorization/google`
        console.log(url)
        if(provider === "google") {
            window.location.href = url
        } else return
    }


  return (
    <div className="flex flex-col items-center justify-center h-screen space-y-4">
        <h1 className="text-xl mb-4">Login Page</h1>
        <button 
            className="bg-white border border-gray-300 px-6 py-2 rounded flex items-center gap-2 cursor-pointer"
            onClick={() => handleLogin("google")}
        >
            <img src="https://developers.google.com/identify/images/g-logo.png" alt="Google" className="w-5 h-5"/>
            Sign in with Google
        </button>
    </div>
  )
}

export default Login