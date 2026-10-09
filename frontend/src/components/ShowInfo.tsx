import {useEffect} from 'react'
import { useAuth } from '../context/AuthContext'
import backend_domain from "../backend_domain"
import axios from 'axios'

const ShowInfo = () => {
    const {user, loading, isAuthenticated} = useAuth()

    useEffect(() => {
        if(!loading && isAuthenticated && user.role === 'ADMIN') {
            getInfo()
        }
    },[loading, isAuthenticated])

    const getInfo = () => { 
        axios.get(`${backend_domain}/api/info`, {withCredentials: true})
        .then(res => {
            console.log("admin info = ", res)
        })
    }
    if(user?.role === 'USER' || user?.role==="ROLE_USER") {
        return <div className="bg-green-500">
            Unsecured user Page
        </div>
    }

  return (
    <div className="bg-amber-300">Secured ADMIN Page</div>
  )
}

export default ShowInfo