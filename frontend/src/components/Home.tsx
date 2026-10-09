import { useAuth } from '../context/AuthContext'
import { Navigate } from 'react-router'
import backend_domain from "../backend_domain";
import axios from "axios"

const Home = () => {
    const {isAuthenticated, loading, user, setUser, logout} = useAuth()

    if(loading) return <div>Loading...</div>
    if(!isAuthenticated && !user) return <Navigate to="/login" replace />

    const handleLogout = async () => {
        logout()
        setUser(null);
    }

    const handleFetchhUser = async () => {
        axios.get(`${backend_domain}/api/user`, {withCredentials: true})
            .then(res => {
                console.log("user info = ", res)
            })
    }

  return (
    <div className="text-center mt-2">
        <div>
            <h1>Welcome, {user?.name}!</h1>
            {user?.picture && <img src={user.picture} alt="Profile" width={80} style={{ borderRadius: "50%" }} />}
            <p><strong>Email:</strong> {user?.email}</p>
            <p><strong>Role:</strong> {user?.role}</p>

            <div>-----------------------------------</div>
            <div>

                <button onClick={handleFetchhUser} className="bg-amber-500">Fetch User</button>
            </div>

            {user?.role && user?.role.includes("ADMIN") && (
                <div style={{ background: "#e0f7fa", padding: "1rem", margin: "1rem 0" }}>
                    <h3>Admin Panel Access Granted</h3>
                    <button onClick={() => window.location.href = "/admin"}>Go to Dashboard</button>
                </div>
            )}

        </div>
        
        <div>
            <button onClick={handleLogout} className='bg-red-500 text-white rounded px-4 py-2 cursor-pointer'>
                Logout
            </button>
        </div>
    </div>
  )
}

export default Home