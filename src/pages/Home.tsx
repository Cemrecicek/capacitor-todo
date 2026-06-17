import {
  IonButton,
  IonFabButton,
  IonIcon,
  IonCheckbox,
  IonContent,
  IonHeader,
  IonInput,
  IonItem,
  IonLabel,
  IonList,
  IonPage,
  IonText,
  IonTitle,
  IonToolbar,
  IonToast,
  IonSearchbar,
} from "@ionic/react";
import { Capacitor, registerPlugin } from "@capacitor/core";
import { add } from "ionicons/icons";
import { Preferences } from "@capacitor/preferences";
import { useCallback, useEffect, useRef, useState } from "react";
import "./Home.css";

type Todo = {
  id: number;
  text: string;
  completed: boolean;
};

type ComposeBridgePlugin = {
  showCompose(options: {
    x: number;
    y: number;
    width: number;
    height: number;
    userName: string;
  }): Promise<void>;

  hideNative(): Promise<void>;
  addListener(
    eventName: "nativeUserName",
    listenerFunc: (data: { userName: string }) => void,
  ): Promise<{ remove: () => Promise<void> }>;
};

const ComposeBridge = registerPlugin<ComposeBridgePlugin>("ComposeBridge");

const STORAGE_KEY = "todos";

const Home: React.FC = () => {
  const platform = Capacitor.getPlatform();

  const [todos, setTodos] = useState<Todo[]>([]);
  const [todoText, setTodoText] = useState("");
  const [toastMessage, setToastMessage] = useState("");
  const [searchText, setSearchText] = useState("");
  const [userName, setUserName] = useState("");

  const nativeBoxRef = useRef<HTMLDivElement>(null);

  const filteredTodos = todos.filter((todo) =>
    todo.text.toLowerCase().includes(searchText.toLowerCase().trim()),
  );

  const syncNativeViewPosition = useCallback(async () => {
    if (!nativeBoxRef.current) return;

    const rect = nativeBoxRef.current.getBoundingClientRect();
    const dpr = window.devicePixelRatio;

    await ComposeBridge.showCompose({
      x: Math.round(rect.left * dpr),
      y: Math.round(rect.top * dpr),
      width: Math.round(rect.width * dpr),
      height: Math.round(rect.height * dpr),
      userName,
    });
  }, [userName]);

  useEffect(() => {
    loadTodos();

    let listenerHandle: { remove: () => Promise<void> } | undefined;

    const setupNativeListener = async () => {
      listenerHandle = await ComposeBridge.addListener(
        "nativeUserName",
        (data) => {
          setUserName(data.userName);
          console.log("Native Compose alanından gelen isim:", data.userName);
        },
      );
    };

    setupNativeListener();
    
    return () => {
      listenerHandle?.remove();
    };
  }, []);

  useEffect(() => {

    window.addEventListener("scroll", syncNativeViewPosition, true);
    window.addEventListener("resize", syncNativeViewPosition);

    const ionContent = document.querySelector("ion-content");
    if (ionContent) {

      ionContent.scrollEvents = true; 
      ionContent.addEventListener("ionScroll", syncNativeViewPosition);
    }

    const intervalId = setInterval(syncNativeViewPosition, 100);

    return () => {
      window.removeEventListener("scroll", syncNativeViewPosition, true);
      window.removeEventListener("resize", syncNativeViewPosition);
      if (ionContent) {
        ionContent.removeEventListener("ionScroll", syncNativeViewPosition);
      }
      clearInterval(intervalId);

      ComposeBridge.hideNative();
    };
  }, [syncNativeViewPosition]);

  const loadTodos = async () => {
    const result = await Preferences.get({ key: STORAGE_KEY });

    if (result.value) {
      setTodos(JSON.parse(result.value));
    }
  };

  const saveTodos = async (newTodos: Todo[]) => {
    setTodos(newTodos);

    await Preferences.set({
      key: STORAGE_KEY,
      value: JSON.stringify(newTodos),
    });
  };

  const addTodo = async () => {
    const trimmedText = todoText.trim();

    if (!trimmedText) {
      setToastMessage("Lütfen bir görev giriniz");
      return;
    }

    const newTodo: Todo = {
      id: Date.now(),
      text: trimmedText,
      completed: false,
    };

    const newTodos = [...todos, newTodo];

    await saveTodos(newTodos);

    setTodoText("");
    setToastMessage("Görev eklendi");

    console.log("Yeni görev eklendi:", newTodo);
  };

  const toggleTodo = async (id: number) => {
    const newTodos = todos.map((todo) =>
      todo.id === id ? { ...todo, completed: !todo.completed } : todo,
    );

    await saveTodos(newTodos);

    const updatedTodo = newTodos.find((todo) => todo.id === id);

    console.log(
      "Görev durumu değişti:",
      updatedTodo?.text,
      updatedTodo?.completed ? "tamamlandı" : "tamamlanmadı",
    );
  };

  const deleteTodo = async (id: number) => {
    const deletedTodo = todos.find((todo) => todo.id === id);
    const newTodos = todos.filter((todo) => todo.id !== id);

    await saveTodos(newTodos);

    setToastMessage("Görev silindi");
    console.log("Görev silindi:", deletedTodo?.text);
  };


  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <div className="header-content">
            <IonTitle className="header-title">Capacitor To Do</IonTitle>

            <div className="header_info">
              <IonText>
                <p className="platform-text">Platform: {platform}</p>
              </IonText>

              {userName ? (
                <IonText>
                  <p className="user-text">Kullanıcı: {userName}</p>
                </IonText>
              ) : (
                <IonText>
                  <p className="user-text">Kullanıcı bilgisi gelmedi</p>
                </IonText>
              )}

            </div>
          </div>
        </IonToolbar>
      </IonHeader>

      <IonContent className="ion-padding">
        <div ref={nativeBoxRef} className="native-compose-placeholder" />

        <IonSearchbar
          value={searchText}
          onIonInput={(event) => setSearchText(event.detail.value ?? "")}
          placeholder="Görev ara"
        />

        <div className="input-container">
          <IonItem className="todo-input-item" lines="none">
            <IonInput
              placeholder="Yeni görev ekle..."
              value={todoText}
              onIonInput={(event) => setTodoText(event.detail.value ?? "")}
              onKeyDown={(event) => {
                if (event.key === "Enter") {
                  addTodo();
                }
              }}
            />
          </IonItem>

          <IonFabButton onClick={addTodo} className="add-button">
            <IonIcon icon={add} />
          </IonFabButton>
        </div>

        {todos.length === 0 ? (
          <IonText color="medium">
            <p className="empty-text">Henüz görev eklenmedi.</p>
          </IonText>
        ) : filteredTodos.length === 0 ? (
          <IonText color="medium">
            <p className="empty-text">Aramana uygun görev bulunamadı.</p>
          </IonText>
        ) : (
          <IonList>
            {filteredTodos.map((todo) => (
              <IonItem key={todo.id}>
                <IonCheckbox
                  checked={todo.completed}
                  onIonChange={() => toggleTodo(todo.id)}
                  slot="start"
                />

                <IonLabel className={todo.completed ? "completed" : ""}>
                  {todo.text}
                </IonLabel>

                <IonButton
                  className="delete-button"
                  fill="solid"
                  slot="end"
                  onClick={() => deleteTodo(todo.id)}
                >
                  Sil
                </IonButton>
              </IonItem>
            ))}
          </IonList>
        )}

        <IonToast
          isOpen={toastMessage !== ""}
          message={toastMessage}
          duration={1500}
          onDidDismiss={() => setToastMessage("")}
        />
      </IonContent>
    </IonPage>
  );
};
export default Home;