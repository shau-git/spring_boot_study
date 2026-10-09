import Login from "./components/Login"
import { Outlet } from "react-router"

const App = () => {
  return (
    <div className="bg-amber-300">
      <Login/>
      <Outlet/>
    </div>
  )
}

export default App