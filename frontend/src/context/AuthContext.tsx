import { createContext , useContext, type ReactNode, useState, useEffect} from "react"
import backend_domain from "../backend_domain";

type User = {
    name: string;
    email: string;
    picture?: string;
    role: 'ROLE_USER' | 'ROLE_ADMIN' | 'USER' | 'ADMIN'
}


type AuthContextType = {
    isAuthenticated: boolean;
    loading: boolean;
    user: User | null;
    setUser: (user: User | null) => void;
    logout: () => void;
}

// initial value
const AuthContext = createContext<AuthContextType>({
    isAuthenticated: false,
    loading: false,
    user: null,
    setUser: () => {},
    logout: () => {}
})

export const useAuth = () => useContext(AuthContext)


type props = {
    children: ReactNode;
}

export const AuthProvider = ({children}: props) => {
    const [user, setUser] = useState<User|null>(null)
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        fetch(`${backend_domain}/api/user`, {
            credentials: "include"  // Automatically attaches HttpOnly JWT cookie
            /*
                Why You Need credentials: "include"
                1. By default, JavaScript fetch() requests will not automatically attach cookies—especially when making cross-origin calls (like frontend on http://localhost:5173 requesting backend on http://localhost:8080)
                2. Because your JWT is marked HttpOnly for security, React cannot manually read the cookie string to set an Authorization header. You must let the browser handle it.
                3. Setting credentials: "include" forces the browser to retrieve that hidden jwt cookie from its storage and automatically place it inside the HTTP Cookie header on every request to Spring Boot.
            
                without credentials: "include":
                1. No Cookie header will be attached
                2. in your backend request.getCookies() will be null
                3. which will cause 401 error.
            */
        })
        .then(res => {
            if (!res.ok) {
                throw new Error("Not authenticated");
            }
            return res.json()
        })
        .then(data => {
            if(data) {
                setUser({
                    name: data.name,
                    email: data.email,
                    picture: data?.picture,
                    role: data.roles
                })
                console.log(data)
            } else {
                setUser(null)
            }
        })
        .catch((e) => {
            console.error(e)
           setUser(null)
        })
        .finally(() => setLoading(false))
    },[])


    const logout = () => {
        // Optional backend call to clear HttpOnly cookie
        fetch(`${backend_domain}/api/auth/logout`, {
            method: "POST",
            credentials: "include"
        }).finally(() => {
            setUser(null);
            window.location.href = "/login";
        });
    };


    const isAuthenticated = !!user

    return (
        <AuthContext.Provider value={{isAuthenticated, loading, user, setUser, logout}}>
            {children}
        </AuthContext.Provider>
    )
}