package eu.leandro.andre.domain.automatic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collection;

import org.junit.Test;
import org.mockito.Mockito;

public class AutomaticExecutionBufferTests {

    private MockedObject mockedObject = Mockito.mock(MockedObject.class);

    @Test
    public void shouldExecuteAnActionWhenBufferReachesItsLimit() {
        int limit = 10;
        AutomaticExecutionBuffer<MockedObject> buffer = createBufferWithSize(limit);
        fillBufferWith(limit, buffer);

        verify(mockedObject, times(1)).save(Mockito.any());
    }

    @Test
    public void shouldExecuteAnActionEveryTimeBufferLimitIsReached() {
        int limit = 20;
        AutomaticExecutionBuffer<MockedObject> buffer = createBufferWithSize(limit);
        fillBufferWith(200, buffer);

        verify(mockedObject, times(10)).save(Mockito.any());
    }

    @Test
    public void shouldDrainOutBufferWhenExecutionIsTriggered() {
        int limit = 100;
        AutomaticExecutionBuffer<MockedObject> buffer = createBufferWithSize(limit);
        fillBufferWith(limit, buffer);

        assertTrue(buffer.retrieveData().isEmpty());
    }

    @Test
    public void shouldDrainOutBufferWhenExecutionIsForced() {
        int limit = 50;
        AutomaticExecutionBuffer<MockedObject> buffer = createBufferWithSize(limit);
        fillBufferWith(limit - 1, buffer);

        assertFalse(buffer.retrieveData().isEmpty());
        buffer.drain();
        assertTrue(buffer.retrieveData().isEmpty());
    }

    @Test
    public void shouldHandleExceptionWhenItWasThrown() {
        AnotherMockedObject aMockedObject = Mockito.mock(AnotherMockedObject.class);

        Mockito.doThrow(IllegalArgumentException.class).when(aMockedObject).save(Mockito.any());

        AutomaticExecutionBuffer<AnotherMockedObject> buffer = AutomaticExecutionBufferBuilder.<AnotherMockedObject>
                execute(aMockedObject::save)
                .whenBufferSizeIs(1)
                .handling(IllegalArgumentException.class).with(()-> exceptionHandler(aMockedObject))
                .build();

        buffer.add(new AnotherMockedObject());

        verify(aMockedObject,times(1)).handleException();
    }

    @Test
    public void shouldHandleSuperExceptionClassWhenItWasThrown() {
        AnotherMockedObject aMockedObject = Mockito.mock(AnotherMockedObject.class);

        Mockito.doThrow(IllegalArgumentException.class).when(aMockedObject).save(Mockito.any());

        AutomaticExecutionBuffer<AnotherMockedObject> buffer = AutomaticExecutionBufferBuilder.<AnotherMockedObject>
                execute(aMockedObject::save)
                .whenBufferSizeIs(1)
                .handling(Exception.class).with(()-> exceptionHandler(aMockedObject))
                .build();

        buffer.add(new AnotherMockedObject());

        verify(aMockedObject,times(1)).handleException();
    }

    @Test
    public void shouldNotThrowExceptionWhenItsHandlerIsNotDeclared() {
        AnotherMockedObject aMockedObject = Mockito.mock(AnotherMockedObject.class);

        Mockito.doThrow(RuntimeException.class).when(aMockedObject).save(Mockito.any());

        AutomaticExecutionBuffer<AnotherMockedObject> buffer = AutomaticExecutionBufferBuilder.<AnotherMockedObject>
                execute(aMockedObject::save)
                .whenBufferSizeIs(1)
                .build();

        buffer.add(new AnotherMockedObject()); //Não deverá lançar exception

        verify(aMockedObject,times(0)).handleException();
    }

    private void exceptionHandler(AnotherMockedObject mockedObject) {
        mockedObject.handleException();
    }

    private void fillBufferWith(int limit, AutomaticExecutionBuffer<MockedObject> buffer) {
        for (int i = 0; i < limit; i++) {
            buffer.add(new MockedObject());
        }
    }

    private AutomaticExecutionBuffer<MockedObject> createBufferWithSize(int limit) {
        AutomaticExecutionBuffer<MockedObject> buffer = AutomaticExecutionBufferBuilder.<MockedObject>
                    execute(mockedObject::save)
                    .whenBufferSizeIs(limit)
                    .build();
        return buffer;
    }

    class MockedObject {
        Collection<MockedObject> save(Collection<MockedObject> arg) {
            return null;
        }
    }

    class AnotherMockedObject {
        void aMethod(String str) {}
        public void handleException() {}
        Collection<AnotherMockedObject> save(Collection<AnotherMockedObject> objs) {
            return null;
        }
    }
}